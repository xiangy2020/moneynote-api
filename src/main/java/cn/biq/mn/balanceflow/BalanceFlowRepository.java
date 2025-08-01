package cn.biq.mn.balanceflow;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;

import cn.biq.mn.account.Account;
import static cn.biq.mn.balanceflow.QBalanceFlow.balanceFlow;
import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.book.Book;
import cn.biq.mn.payee.Payee;
import cn.biq.mn.user.User;

@Repository
public interface BalanceFlowRepository extends BaseRepository<BalanceFlow>  {

    boolean existsByAccountOrTo(Account account, Account to);

    boolean existsByBook(Book book);

    long countByCreatorAndInsertAtBetween(User creator, long start, long end);

    boolean existsByPayee(Payee payee);

    List<BalanceFlow> findAllByBookOrderByCreateTimeDesc(Book book);

    default List<DailyBalance> findDailyBalancesByAccount(Integer accountId) {
        return getJpaQueryFactory()
                .select(
                        Projections.constructor(
                                DailyBalance.class,
                                Expressions.stringTemplate( "FUNCTION('FROM_UNIXTIME', {0}/1000, '%Y-%m-%d')", balanceFlow.createTime),
                                Expressions.cases()
                                .when(balanceFlow.type.eq(FlowType.EXPENSE))
                                        .then(balanceFlow.amount.negate())
                                .otherwise(balanceFlow.amount)
                                .sum()  // 对条件表达式结果求和
                        )
                )
                .from(balanceFlow)
                .where(balanceFlow.account.id.eq(accountId))
                .groupBy(Expressions.stringTemplate("FUNCTION('FROM_UNIXTIME', {0}/1000, '%Y-%m-%d')", balanceFlow.createTime))
                .fetch();
    }
}
