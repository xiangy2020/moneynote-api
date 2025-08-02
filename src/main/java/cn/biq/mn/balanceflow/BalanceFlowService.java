package cn.biq.mn.balanceflow;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import cn.biq.mn.account.Account;
import cn.biq.mn.account.AccountRepository;
import cn.biq.mn.base.BaseService;
import cn.biq.mn.book.Book;
import cn.biq.mn.categoryrelation.CategoryRelation;
import cn.biq.mn.categoryrelation.CategoryRelationForm;
import cn.biq.mn.categoryrelation.CategoryRelationService;
import cn.biq.mn.exception.FailureMessageException;
import cn.biq.mn.exception.ItemNotFoundException;
import cn.biq.mn.flowfile.FlowFile;
import cn.biq.mn.flowfile.FlowFileDetails;
import cn.biq.mn.flowfile.FlowFileMapper;
import cn.biq.mn.flowfile.FlowFileRepository;
import cn.biq.mn.group.Group;
import cn.biq.mn.payee.Payee;
import cn.biq.mn.payee.PayeeRepository;
import cn.biq.mn.tagrelation.TagRelation;
import cn.biq.mn.tagrelation.TagRelationService;
import cn.biq.mn.user.User;
import cn.biq.mn.utils.ExcelUtils;
import cn.biq.mn.utils.Limitation;
import cn.biq.mn.utils.MyCollectionUtil;
import cn.biq.mn.utils.SessionUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BalanceFlowService {

    private final BalanceFlowRepository balanceFlowRepository;
    private final SessionUtil sessionUtil;
    private final AccountRepository accountRepository;
    private final BaseService baseService;
    private final PayeeRepository payeeRepository;
    private final CategoryRelationService categoryRelationService;
    private final TagRelationService tagRelationService;
    private final BalanceFlowMapper balanceFlowMapper;
    private final FlowFileRepository flowFileRepository;

    private void checkBeforeAdd(BalanceFlowAddForm form, Book book, User user) {
        // 对用户添加账单的操作进行限流。
//        Long[] day = CalendarUtil.getIn1Day();
//        if (balanceFlowRepository.countByCreatorAndInsertAtBetween(user, day[0], day[1]) > Limitation.flow_max_count_daily) {
//            throw new FailureMessageException("add.flow.daily.overflow");
//        }
        Account account = null;
        if (form.getAccount() != null) {
            account = baseService.findAccountById(form.getAccount());
        }
        if (form.getType() == FlowType.EXPENSE || form.getType() == FlowType.INCOME) {
            // 支出和收入的分类不能为空，因为transfer可以为空，所以只能在这里验证
            if (CollectionUtils.isEmpty(form.getCategories())) {
                throw new FailureMessageException("add.flow.category.empty");
            }
            // 检查Category不能重复
            if (MyCollectionUtil.hasDuplicate(form.getCategories())) {
                throw new FailureMessageException("add.flow.category.duplicated");
            }
            // 限制每个账单的分类数目
            if (form.getCategories().size() > Limitation.flow_max_category_count) {
                throw new FailureMessageException("add.flow.category.overflow");
            }
            // 外币账户必须输入convertedAmount
            if (account != null) {
                if (!account.getCurrencyCode().equals(book.getDefaultCurrencyCode())) {
                    form.getCategories().forEach(i -> {
                        if (i.getConvertedAmount() == null) throw new FailureMessageException("valid.fail");
                    });
                }
            }
        }
        if (form.getType() == FlowType.TRANSFER) {
            // 转账必须有account, to id和amount
            if (account == null || form.getTo() == null || form.getAmount() == null) {
                throw new FailureMessageException("valid.fail");
            }
            // 转账的两个账户的币种不同，必须输入convertedAmount
            Account toAccount = baseService.findAccountById(form.getTo());
            if (!account.getCurrencyCode().equals(toAccount.getCurrencyCode())) {
                if (form.getConvertedAmount() == null) {
                    throw new FailureMessageException("valid.fail");
                }
            }
        }
    }

    public boolean add(BalanceFlowAddForm form) {
        User user = sessionUtil.getCurrentUser();
        Group group = sessionUtil.getCurrentGroup();
        Book book = baseService.getBookInGroup(form.getBook());
        checkBeforeAdd(form, book, user);
        BalanceFlow entity = BalanceFlowMapper.toEntity(form);
        entity.setGroup(group);
        entity.setBook(book);
        entity.setCreator(user);
        if (form.getAccount() != null) {
            entity.setAccount(baseService.findAccountById(form.getAccount()));
        }
        if (form.getType() == FlowType.EXPENSE || form.getType() == FlowType.INCOME) {
            BigDecimal amount = form.getCategories().stream().map(CategoryRelationForm::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            entity.setAmount(amount);
            if (entity.getAccount() == null || entity.getAccount().getCurrencyCode().equals(book.getDefaultCurrencyCode())) {
                entity.setConvertedAmount(amount);
            } else {
                BigDecimal convertedAmount = form.getCategories().stream().map(CategoryRelationForm::getConvertedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                entity.setConvertedAmount(convertedAmount);
            }
            categoryRelationService.addRelation(form.getCategories(), entity, book, entity.getAccount());
        }
        if (form.getType() == FlowType.TRANSFER) {
            Account toAccount = baseService.findAccountById(form.getTo());
            entity.setTo(toAccount);
            if (entity.getAccount().getCurrencyCode().equals(entity.getTo().getCurrencyCode())) {
                entity.setConvertedAmount(entity.getAmount());
            } else {
                entity.setConvertedAmount(form.getConvertedAmount());
            }
        }
        if (form.getTags() != null) {
            tagRelationService.addRelation(form.getTags(), entity, book, entity.getAccount());
        }
        if (form.getType() == FlowType.EXPENSE || form.getType() == FlowType.INCOME) {
            if (form.getPayee() != null) {
                Payee payee = payeeRepository.findOneByBookAndId(book, form.getPayee()).orElseThrow(ItemNotFoundException::new);
                entity.setPayee(payee);
            }
        }
        balanceFlowRepository.save(entity);
        if (form.getConfirm()) {
            confirmBalance(entity);
        }
        return true;
    }

    // 余额确认，包括账户扣款。
    private void confirmBalance(BalanceFlow flow) {
        if (flow.getConfirm() && flow.getAccount() != null) {
            Account account = flow.getAccount();
            if (flow.getType() == FlowType.EXPENSE) {
                account.setBalance(account.getBalance().subtract(flow.getAmount()));
            } else if (flow.getType() == FlowType.INCOME) {
                account.setBalance(account.getBalance().add(flow.getAmount()));
            } else if (flow.getType() == FlowType.TRANSFER) {
                Account toAccount = flow.getTo();
                BigDecimal amount = flow.getAmount();
                BigDecimal convertedAmount = flow.getConvertedAmount();
                account.setBalance(account.getBalance().subtract(amount));
                toAccount.setBalance(toAccount.getBalance().add(convertedAmount));
                accountRepository.save(toAccount);
            }
            accountRepository.save(account);
        }
    }

    private void refundBalance(BalanceFlow flow) {
        if (flow.getConfirm() && flow.getAccount() != null) {
            Account account = flow.getAccount();
            if (flow.getType() == FlowType.EXPENSE) {
                account.setBalance(account.getBalance().add(flow.getAmount()));
            } else if (flow.getType() == FlowType.INCOME) {
                account.setBalance(account.getBalance().subtract(flow.getAmount()));
            } else if (flow.getType() == FlowType.TRANSFER) {
                Account toAccount = flow.getTo();
                BigDecimal amount = flow.getAmount();
                BigDecimal convertedAmount = flow.getConvertedAmount();
                account.setBalance(account.getBalance().add(amount));
                toAccount.setBalance(toAccount.getBalance().subtract(convertedAmount));
                accountRepository.save(toAccount);
            } else if (flow.getType() == FlowType.ADJUST) {
                account.setBalance(account.getBalance().subtract(flow.getAmount()));
            }
            accountRepository.save(account);
        }
    }

    @Transactional(readOnly = true)
    // 不加transaction会报错
    // org.hibernate.LazyInitializationException: could not initialize proxy [tech.jiukuai.bookkeeping.user.account.Account#1] - no Session
    public Page<BalanceFlowDetails> query(BalanceFlowQueryForm request, Pageable page) {
        Group group = sessionUtil.getCurrentGroup();
        Page<BalanceFlow> entityPage = balanceFlowRepository.findAll(request.buildPredicate(group), page);
        return entityPage.map(balanceFlowMapper::toDetails);
    }

    @Transactional(readOnly = true)
    public BalanceFlowDetails get(int id) {
        return balanceFlowMapper.toDetails(baseService.findFlowById(id));
    }

    @Transactional(readOnly = true)
    public BigDecimal[] statistics(BalanceFlowQueryForm request) {
        var result = new BigDecimal[3];
        Group group = sessionUtil.getCurrentGroup();
        QBalanceFlow balanceFlow = QBalanceFlow.balanceFlow;
        result[0] = balanceFlowRepository.calcSum(request.buildExpensePredicate(group), balanceFlow.convertedAmount, balanceFlow);
        result[1] = balanceFlowRepository.calcSum(request.buildIncomePredicate(group), balanceFlow.convertedAmount, balanceFlow);
        result[2] = result[1].subtract(result[0]);
        return result;
    }

    public boolean remove(int id) {
        BalanceFlow entity = baseService.findFlowById(id);
        refundBalance(entity);
        // 要先删除文件
        flowFileRepository.deleteByFlow(entity);
        balanceFlowRepository.delete(entity);
        return true;
    }

    private boolean categoryEquals(Set<CategoryRelation> categories1, List<CategoryRelationForm> categories2) {
        if (categories1.size() != categories2.size()) {
            return false;
        }
        for (var i : categories1) {
            boolean found = false;
            for (var j : categories2) {
                if (j.equals(i)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private boolean tagEquals(Set<Integer> tags1, Set<TagRelation> tags2) {
        if (tags1.size() != tags2.size()) {
            return false;
        }
        for (var i : tags2) {
            boolean found = false;
            for (var j : tags1) {
                if (j.equals(i.getTag().getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    // 不能修改相关账户和金额，不能修改分类
    public boolean update(Integer id, BalanceFlowUpdateForm form) {
        // checkBeforeUpdate
        BalanceFlow entity = baseService.findFlowById(id);
        Book book = entity.getBook();
        BalanceFlowMapper.updateEntity(form, entity);
        if (form.getPayee() != null) {
            if (entity.getPayee() == null || !form.getPayee().equals(entity.getPayee().getId())) {
                Payee payee = payeeRepository.findOneByBookAndId(book, form.getPayee()).orElseThrow(ItemNotFoundException::new);
                entity.setPayee(payee);
            }
        } else {
            entity.setPayee(null);
        }
        // 传入null代表不修改，传入空数组[]，代表清空。
        if (form.getTags() != null && !tagEquals(form.getTags(), entity.getTags())) {
            entity.getTags().clear();
            tagRelationService.addRelation(form.getTags(), entity, book, entity.getAccount());
        }
        balanceFlowRepository.save(entity);
        return true;
    }

    public boolean confirm(Integer id) {
        BalanceFlow entity = baseService.findFlowById(id);
        entity.setConfirm(true);
        confirmBalance(entity);
        balanceFlowRepository.save(entity);
        return true;
    }

    public FlowFileDetails addFile(Integer id, MultipartFile file) {
        FlowFile flowFile = new FlowFile();
        try {
            flowFile.setData(file.getBytes());
        } catch (IOException e) {
            throw new FailureMessageException("add.flow.file.fail");
        }
        flowFile.setCreator(sessionUtil.getCurrentUser());
        flowFile.setFlow(baseService.findFlowById(id));
        flowFile.setCreateTime(System.currentTimeMillis());
        flowFile.setSize(file.getSize());
        flowFile.setContentType(file.getContentType());
        flowFile.setOriginalName(file.getOriginalFilename());
        flowFileRepository.save(flowFile);
        return FlowFileMapper.toDetails(flowFile);
    }

    public List<FlowFileDetails> getFiles(Integer id) {
        BalanceFlow flow = baseService.findFlowById(id);
        List<FlowFile> flowFiles = flowFileRepository.findByFlow(flow);
        return flowFiles.stream().map(FlowFileMapper::toDetails).toList();
    }

    @Transactional
    public boolean importFromExcel(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Get header row to find column indices
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnIndices = new HashMap<>();
            for (Cell cell : headerRow) {
                columnIndices.put(cell.getStringCellValue().toLowerCase(), cell.getColumnIndex());
            }

            // Validate required columns
            List<String> requiredColumns = Arrays.asList("create_time", "amount", "type", "book_id", "account_id", "group_id");
            for (String column : requiredColumns) {
                if (!columnIndices.containsKey(column.toLowerCase())) {
                    throw new IllegalArgumentException("Required column '" + column + "' not found in the Excel file.");
                }
            }

            // Skip header row
            // if (rowIterator.hasNext()) {
            //     rowIterator.next();
            // }

            boolean skipFirstRow = true;

            while (rowIterator.hasNext()) {
                

                Row row = rowIterator.next();
                
                if (skipFirstRow) {
                    skipFirstRow = false; // 跳过第一行
                    continue; // 跳过当前迭代
                }

                System.out.println("Processing row: " + row.getRowNum());
                // Parse required fields
                long createTime = DateUtil.getJavaDate(row.getCell(columnIndices.get("create_time")).getNumericCellValue()).getTime();
                double amountValue = row.getCell(columnIndices.get("amount")).getNumericCellValue();
                BigDecimal amount = BigDecimal.valueOf(amountValue);
                int typeValue = (int) row.getCell(columnIndices.get("type")).getNumericCellValue();
                FlowType type = FlowType.fromCode(typeValue);
                int bookId = (int) row.getCell(columnIndices.get("book_id")).getNumericCellValue();
                // int creatorId = (int) row.getCell(columnIndices.get("creator_id")).getNumericCellValue();

                int groupId = (int) row.getCell(columnIndices.get("group_id")).getNumericCellValue();
                int creatorId = ExcelUtils.getSafeIntCellValue(row, columnIndices, "creator_id", 1);
                // Parse optional fields
                // boolean confirm = row.getCell(columnIndices.getOrDefault("confirm", -1)) != null && 
                //                   row.getCell(columnIndices.getOrDefault("confirm", -1)).getBooleanCellValue();
                boolean confirm = ExcelUtils.getSafeBooleanCellValue(row, columnIndices, "confirm", false); 

                // double convertedAmountValue = row.getCell(columnIndices.getOrDefault("converted_amount", -1)) != null ? 
                //                         row.getCell(columnIndices.getOrDefault("converted_amount", -1)).getNumericCellValue() : 0.0;

                double convertedAmountValue = ExcelUtils.getSafeDoubleCellValue(row, columnIndices, "converted_amount", 0.0);
                BigDecimal convertedAmount = BigDecimal.valueOf(convertedAmountValue);
                boolean include = ExcelUtils.getSafeBooleanCellValue(row, columnIndices, "include", false); 
                // boolean include = row.getCell(columnIndices.getOrDefault("include", -1)) != null && 
                //                  row.getCell(columnIndices.getOrDefault("include", -1)).getBooleanCellValue();
                long insertAt = System.currentTimeMillis();
                // String notes = row.getCell(columnIndices.getOrDefault("notes", -1)) != null ? 
                //                row.getCell(columnIndices.getOrDefault("notes", -1)).getStringCellValue() : "";
                String notes = ExcelUtils.getSafeStringCellValue(row, columnIndices, "notes", "");
                // String title = row.getCell(columnIndices.getOrDefault("title", -1)) != null ? 
                //                row.getCell(columnIndices.getOrDefault("title", -1)).getStringCellValue() : "";
                String title = ExcelUtils.getSafeStringCellValue(row, columnIndices, "title", "");
                int accountId = ExcelUtils.getSafeIntCellValue(row, columnIndices, "accountId", 1);
                // int accountId = row.getCell(columnIndices.getOrDefault("account_id", -1)) != null ? 
                //                 (int) row.getCell(columnIndices.getOrDefault("account_id", -1)).getNumericCellValue() : 0;
                int payeeId = ExcelUtils.getSafeIntCellValue(row, columnIndices, "payeeId", 1);
                // int payeeId = row.getCell(columnIndices.getOrDefault("payee_id", -1)) != null ? 
                //               (int) row.getCell(columnIndices.getOrDefault("payee_id", -1)).getNumericCellValue() : 0;
                // int toId = row.getCell(columnIndices.getOrDefault("to_id", -1)) != null ? 
                //            (int) row.getCell(columnIndices.getOrDefault("to_id", -1)).getNumericCellValue() : 0;
                
                // Create and save BalanceFlow entity
                BalanceFlow balanceFlow = new BalanceFlow();
                balanceFlow.setCreateTime(createTime);
                balanceFlow.setAmount(amount);
                balanceFlow.setType(type);
                balanceFlow.setBookId(bookId);
                balanceFlow.setCreatorId(creatorId);
                balanceFlow.setGroupId(groupId);
                balanceFlow.setConfirm(confirm);
                balanceFlow.setConvertedAmount(convertedAmount);
                balanceFlow.setInclude(include);
                balanceFlow.setInsertAt(insertAt);
                balanceFlow.setNotes(notes);
                balanceFlow.setTitle(title);
                balanceFlow.setAccountId(accountId);
                balanceFlow.setPayeeId(payeeId);
                // balanceFlow.setToId(toId);
                
                balanceFlowRepository.save(balanceFlow);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to import Excel file", e);
        }
    }

}
