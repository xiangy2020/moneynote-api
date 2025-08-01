package cn.biq.mn.report;

import cn.biq.mn.base.BaseController;
import cn.biq.mn.category.CategoryType;
import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import cn.biq.mn.balanceflow.BalanceFlowQueryForm;
import cn.biq.mn.balanceflow.FlowType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController extends BaseController {

    private final ReportService reportService;

    @RequestMapping(method = RequestMethod.GET, value = "expense-category")
    public BaseResponse handleExpenseCategory(@Valid CategoryReportQueryForm form) {
        return new DataResponse<>(reportService.reportCategory(form, CategoryType.EXPENSE));
    }

    @RequestMapping(method = RequestMethod.GET, value = "income-category")
    public BaseResponse handleIncomeCategory(@Valid CategoryReportQueryForm form) {
        return new DataResponse<>(reportService.reportCategory(form, CategoryType.INCOME));
    }

    @RequestMapping(method = RequestMethod.GET, value = "expense-tag")
    public BaseResponse handleExpenseTag(@Valid CategoryReportQueryForm form) {
        return new DataResponse<>(reportService.reportTag(form, FlowType.EXPENSE));
    }

    @RequestMapping(method = RequestMethod.GET, value = "income-tag")
    public BaseResponse handleIncomeTag(@Valid CategoryReportQueryForm form) {
        return new DataResponse<>(reportService.reportTag(form, FlowType.INCOME));
    }

    @RequestMapping(method = RequestMethod.GET, value = "expense-payee")
    public BaseResponse handleExpensePayee(@Valid BalanceFlowQueryForm form) {
        return new DataResponse<>(reportService.reportPayee(form, FlowType.EXPENSE));
    }

    @RequestMapping(method = RequestMethod.GET, value = "income-payee")
    public BaseResponse handleIncomePayee(@Valid BalanceFlowQueryForm form) {
        return new DataResponse<>(reportService.reportPayee(form, FlowType.INCOME));
    }

    @RequestMapping(method = RequestMethod.GET, value = "balance")
    public BaseResponse handleBalance() {
        return new DataResponse<>(reportService.reportBalance());
    }

    @RequestMapping(method = RequestMethod.GET, value = "daily-balance")
    public BaseResponse handleDailyBalance() {
        return new DataResponse<>(reportService.reportDailyBalance());
    }
}
