package cn.biq.mn.report;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import cn.biq.mn.account.AccountService;
import cn.biq.mn.base.BaseTest;
import cn.biq.mn.group.Group;
import cn.biq.mn.utils.SessionUtil;

public class ReportServiceTest extends BaseTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private Group group;

    @Mock
    private SessionUtil sessionUtil;

    @Mock
    private AccountService accountService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(group.getDefaultCurrencyCode()).thenReturn("USD");
        when(sessionUtil.getCurrentGroup()).thenReturn(group);
        when(accountService.getAssets()).thenReturn(new ArrayList<>());
        when(accountService.getDebts()).thenReturn(new ArrayList<>());
    }

    @Test
    public void testReportDailyBalance() {
        var result = reportService.reportDailyBalance();
        assertNotNull(result);
    }
}
