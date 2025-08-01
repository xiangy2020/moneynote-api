package cn.biq.mn.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import cn.biq.mn.account.Account;
import cn.biq.mn.account.AccountService;
import cn.biq.mn.balanceflow.BalanceFlow;
import cn.biq.mn.balanceflow.BalanceFlowQueryForm;
import cn.biq.mn.balanceflow.BalanceFlowRepository;
import cn.biq.mn.balanceflow.DailyBalance;
import cn.biq.mn.balanceflow.FlowType;
import cn.biq.mn.base.BaseService;
import cn.biq.mn.book.Book;
import cn.biq.mn.category.Category;
import cn.biq.mn.category.CategoryRepository;
import cn.biq.mn.category.CategoryType;
import cn.biq.mn.categoryrelation.CategoryRelation;
import cn.biq.mn.categoryrelation.CategoryRelationRepository;
import cn.biq.mn.currency.CurrencyService;
import cn.biq.mn.group.Group;
import cn.biq.mn.payee.Payee;
import cn.biq.mn.payee.PayeeRepository;
import cn.biq.mn.tag.Tag;
import cn.biq.mn.tag.TagRepository;
import cn.biq.mn.tagrelation.TagRelation;
import cn.biq.mn.tagrelation.TagRelationRepository;
import cn.biq.mn.tree.TreeUtils;
import cn.biq.mn.utils.SessionUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final SessionUtil sessionUtil;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PayeeRepository payeeRepository;
    private final CategoryRelationRepository categoryRelationRepository;
    private final TagRelationRepository tagRelationRepository;
    private final BalanceFlowRepository balanceFlowRepository;
    private final AccountService accountService;
    private final CurrencyService currencyService;
    private final BaseService baseService;

    public List<ChartVO> reportCategory(CategoryReportQueryForm form, CategoryType type) {
        List<ChartVO> result = new ArrayList<>();
        Book book = baseService.getBookInGroup(form.getBook());
        List<Category> categories = categoryRepository.findAllByBookAndType(book, type);
        Category requestCategory = null;
        List<Category> rootCategories = new ArrayList<>();
        if (CollectionUtils.isEmpty(form.getCategories())) {
            rootCategories = categories.stream().filter(i->i.getLevel() == 0).collect(Collectors.toList());
        } else {
            if (form.getCategories().size() == 1) {
                requestCategory = categories.stream().filter(i-> i.getId().equals(form.getCategories().iterator().next())).toList().get(0);
                rootCategories = TreeUtils.getChildren(requestCategory, categories);
            } else {
                for (Integer id : form.getCategories()) {
                    // 传入的id不存在，会报index异常。
                    Category category = categories.stream().filter(i-> i.getId().equals(id)).toList().get(0);
                    rootCategories.add(category);
                }
            }
        }
        List<CategoryRelation> categoryRelations = categoryRelationRepository.findAll(form.buildCategoryPredicate(book));
        for (Category i : rootCategories) {
            ChartVO vo = new ChartVO();
            vo.setX(i.getName());
            List<Category> offSpringCategory = TreeUtils.getOffspring(i, categories);
            List<Integer> offSpringCategoryIds = offSpringCategory.stream().map(j->j.getId()).collect(Collectors.toList());
            offSpringCategoryIds.add(i.getId());
            vo.setY(categoryRelations.stream().filter(j -> offSpringCategoryIds.contains(j.getCategory().getId())).map(CategoryRelation::getConvertedAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            if (vo.getY().signum() != 0) {
                result.add(vo);
            }
        }
        if (!CollectionUtils.isEmpty(form.getCategories()) && form.getCategories().size() == 1) {
            ChartVO vo = new ChartVO();
            vo.setX(requestCategory.getName());
            vo.setY(categoryRelations.stream().filter(i -> i.getCategory().getId().
                    equals(form.getCategories().iterator().next())).
                    map(CategoryRelation::getConvertedAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            if (vo.getY().signum() != 0) {
                result.add(vo);
            }
        }
        result.sort(Comparator.comparing(ChartVO::getY).reversed());
        BigDecimal total = result.stream().map(ChartVO::getY).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.signum() == 0) {
            return new ArrayList<>();
        }
        result.forEach(vo -> vo.setPercent(vo.getY().multiply(new BigDecimal(100)).divide(total, 2, RoundingMode.HALF_UP)));
        return result;
    }

    public List<ChartVO> reportTag(CategoryReportQueryForm form, FlowType type) {
        List<ChartVO> result = new ArrayList<>();
        Book book = baseService.getBookInGroup(form.getBook());
        List<Tag> tags = new ArrayList<>();
        if (type == FlowType.EXPENSE) {
            tags = tagRepository.findByBookAndEnableAndCanExpense(book, true, true);
        } else if (type == FlowType.INCOME) {
            tags = tagRepository.findByBookAndEnableAndCanIncome(book, true, true);
        }
        Tag requestTag = null;
        List<Tag> rootTags = new ArrayList<>();
        if (CollectionUtils.isEmpty(form.getTags())) {
            rootTags = tags.stream().filter(i->i.getLevel() == 0).collect(Collectors.toList());
        } else {
            if (form.getTags().size() == 1) {
                requestTag = tags.stream().filter(i-> i.getId().equals(form.getTags().iterator().next())).toList().get(0);
                rootTags = TreeUtils.getChildren(requestTag, tags);
            } else {
                for (Integer id : form.getTags()) {
                    // 传入的id不存在，会报index异常。
                    Tag tag = tags.stream().filter(i-> i.getId().equals(id)).toList().get(0);
                    rootTags.add(tag);
                }
            }
        }
        List<TagRelation> tagRelations = tagRelationRepository.findAll(form.buildTagPredicate(book, type));
        for (Tag i : rootTags) {
            ChartVO vo = new ChartVO();
            vo.setX(i.getName());
            List<Tag> offSpringTag = TreeUtils.getOffspring(i, tags);
            List<Integer> offSpringTagIds = offSpringTag.stream().map(j->j.getId()).collect(Collectors.toList());
            offSpringTagIds.add(i.getId());
            vo.setY(tagRelations.stream().filter(j -> offSpringTagIds.contains(j.getTag().getId())).map(TagRelation::getConvertedAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            if (vo.getY().signum() != 0) {
                result.add(vo);
            }
        }
        // 查询单个标签，上面会把他的子标签统计上，但是还要把自己的的也统计上
        if (!CollectionUtils.isEmpty(form.getTags()) && form.getTags().size() == 1) {
            ChartVO vo = new ChartVO();
            vo.setX(requestTag.getName());
            vo.setY(tagRelations.stream().filter(i -> i.getTag().getId().
                            equals(form.getTags().iterator().next())).
                    map(TagRelation::getConvertedAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            if (vo.getY().signum() != 0) {
                result.add(vo);
            }
        }
        result.sort(Comparator.comparing(ChartVO::getY).reversed());
        BigDecimal total = result.stream().map(ChartVO::getY).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.signum() == 0) {
            return new ArrayList<>();
        }
        result.forEach(vo -> vo.setPercent(vo.getY().multiply(new BigDecimal(100)).divide(total, 2, RoundingMode.HALF_UP)));
        return result;
    }

    public List<ChartVO> reportPayee(BalanceFlowQueryForm form, FlowType type) {
        List<ChartVO> result = new ArrayList<>();
        Book book = baseService.getBookInGroup(form.getBook());
        List<Payee> payees = new ArrayList<>();
        if (type == FlowType.EXPENSE) {
            payees = payeeRepository.findByBookAndEnableAndCanExpense(book, true, true);
        } else if (type == FlowType.INCOME) {
            payees = payeeRepository.findByBookAndEnableAndCanIncome(book, true, true);
        }
        List<Payee> rootPayees = new ArrayList<>();
        if (CollectionUtils.isEmpty(form.getPayees())) {
            rootPayees = payees;
        } else {
            for (Integer id : form.getPayees()) {
                // 传入的id不存在，会报index异常。
                Payee payee = payees.stream().filter(i-> i.getId().equals(id)).toList().get(0);
                rootPayees.add(payee);
            }
        }
        Group group = sessionUtil.getCurrentGroup();
        List<BalanceFlow> balanceFlows = balanceFlowRepository.findAll(form.buildPredicate(group));
        for (Payee i : rootPayees) {
            ChartVO vo = new ChartVO();
            vo.setX(i.getName());
            vo.setY(balanceFlows.stream().filter(j -> i.getId().equals((j.getPayee() != null) ? j.getPayee().getId() : null)).map(BalanceFlow::getConvertedAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            if (vo.getY().signum() != 0) {
                result.add(vo);
            }
        }
        result.sort(Comparator.comparing(ChartVO::getY).reversed());
        BigDecimal total = result.stream().map(ChartVO::getY).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.signum() == 0) {
            return new ArrayList<>();
        }
        result.forEach(vo -> vo.setPercent(vo.getY().multiply(new BigDecimal(100)).divide(total, 2, RoundingMode.HALF_UP)));
        return result;
    }

    public List<List<ChartVO>> reportBalance() {
        List<List<ChartVO>> result = new ArrayList<>();
        Group group = sessionUtil.getCurrentGroup();

        List<ChartVO> assetChart = new ArrayList<>();
        List<Account> assetAccounts = accountService.getAssets();
        for (Account account : assetAccounts) {
            if (account.getBalance().signum() != 0) {
                ChartVO vo = new ChartVO();
                vo.setX(account.getName());
                vo.setY(currencyService.convert(account.getBalance(), account.getCurrencyCode(), group.getDefaultCurrencyCode()));
                assetChart.add(vo);
            }
        }
        assetChart.sort(Comparator.comparing(ChartVO::getY).reversed());
        BigDecimal total1 = assetChart.stream().map(ChartVO::getY).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total1.signum() == 0) {
            assetChart = new ArrayList<>();
        } else {
            assetChart.forEach(vo -> vo.setPercent(vo.getY().multiply(new BigDecimal(100)).divide(total1, 2, RoundingMode.HALF_UP)));
        }
        List<ChartVO> debtChart = new ArrayList<>();
        List<Account> debtAccounts = accountService.getDebts();
        for (Account account : debtAccounts) {
            if (account.getBalance().signum() != 0) {
                ChartVO vo = new ChartVO();
                vo.setX(account.getName());
                vo.setY(currencyService.convert(account.getBalance(), account.getCurrencyCode(), group.getDefaultCurrencyCode()).negate());
                debtChart.add(vo);
            }
        }
        debtChart.sort(Comparator.comparing(ChartVO::getY).reversed());
        BigDecimal total2 = debtChart.stream().map(ChartVO::getY).reduce(BigDecimal.ZERO, BigDecimal::add);
        debtChart.forEach(vo -> vo.setPercent(vo.getY().multiply(new BigDecimal(100)).divide(total2, 2, RoundingMode.HALF_UP)));

        result.add(assetChart);
        result.add(debtChart);
        return result;
    }

    public List<ChartVO> reportDailyBalance() {
        Group group = sessionUtil.getCurrentGroup();
        if (group == null) {
            throw new IllegalStateException("Current group is not available");
        }
        List<Account> accounts = accountService.getAssets();
        accounts.addAll(accountService.getDebts());
        Map<String, BigDecimal> dailyBalances = getDailyBalances(accounts, group.getDefaultCurrencyCode());

        List<ChartVO> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : dailyBalances.entrySet()) {
            ChartVO vo = new ChartVO();
            vo.setX(entry.getKey());
            vo.setY(entry.getValue());
            result.add(vo);
        }

        result.sort(Comparator.comparing(ChartVO::getX));
        return result;
    }

    private Map<String, BigDecimal> getDailyBalances(List<Account> accounts, String currencyCode) {
        Map<String, BigDecimal> dailyBalances = new HashMap<>();
        Group group = sessionUtil.getCurrentGroup();
        
        for (Account account : accounts) {
            List<DailyBalance> balances = balanceFlowRepository.findDailyBalancesByAccount(account.getId());
            for (DailyBalance balance : balances) {
                if (balance.getDate() != null) {
                    String date = balance.getDate().toString();
                    BigDecimal convertedAmount = currencyService.convert(balance.getAmount(), account.getCurrencyCode(), currencyCode);
                    dailyBalances.merge(date, convertedAmount, BigDecimal::add);
                }
            }
        }
        
        return dailyBalances;
    }

}
