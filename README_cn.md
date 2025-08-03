# 九快记账后台API


## 系统介绍

一个开源免费的记账解决方案，包括[后端](https://github.com/getmoneynote/moneynote-api)，[网页版](https://github.com/getmoneynote/moneywhere-user-fe)，[App](https://github.com/getmoneynote/moneywhere_user_flutter)，主要用于个人生活记账，开店收支记账，支持[docker一键部署](https://github.com/getmoneynote/docker-compose-moneywhere)自己的记账程序。独立运行，不依赖任何第三方服务。

## 个人部署
请参考[docker compose](https://github.com/getmoneynote/docker-compose-moneywhere)项目

![Screen](https://raw.githubusercontent.com/getmoneynote/moneynote-api/main/screencapture.png "Screen Shot")


![Struct](https://raw.githubusercontent.com/getmoneynote/docker-compose-moneywhere/main/struct.png "Struct")

[PC示例站点](https://demo.moneywhere.com)  注册邀请码 111111（6个1）

[手机示例站点](https://mdemo.moneywhere.com)   api地址：https://mdemo.moneywhere.com/api/v1/, 用pc站点注册的账号登录。

## 使用文档
[从零开始搭建完善的记账体系](https://sspai.com/post/58025)

[https://help.moneywhere.com](https://help.moneywhere.com)

[B站视频](https://www.bilibili.com/video/BV1vz4y1i7dz)

如遇到任何问题欢迎加入 QQ群: 639653091 讨论。

## 技术方案
采用前后端分离模式，[后端](https://github.com/getmoneynote/moneynote-api)使用Spring Boot 3框架，[网页版](https://github.com/getmoneynote/moneywhere-user-fe)使用Antd Pro，[App](https://github.com/getmoneynote/moneywhere_user_flutter)使用Flutter和Bloc。

## 主要功能

- 监控个人资产负债
- 记录个人支出和收入
- 支持账单添加多个附件
- 支持多个账本记账
- 支持多币种
- 支持多种账本模板
- 支持账本的分类和标签管理
- 分组多用户记账
- 报表统计
- 支持docker compose一键部署

## ToDo
- 预算功能
- 汇率和账本模板服务downgrade
- 完善代码最佳实践
- 完善分组功能
- 开发微信小程序


## 20250801新增首页展示账户的每日变动折线图
1.每日账户余额曲线图
1. 实现了`getDailyBalances`方法，用于汇总每日余额数据
2. 在`BalanceFlowRepository`中添加了`findDailyBalancesByAccount`方法，使用QueryDSL查询每日余额
3. 在`BaseRepository`和`BaseRepositoryImpl`中添加了`getJpaQueryFactory`方法支持
4. 在`ReportService`中添加了`reportDailyBalance`方法
5. 在`ReportController`中添加了新的API接口`/reports/daily-balance`
6. 确认`ChartVO`类已经满足需求
测试用例
已创建测试类ReportServiceTest，添加了testReportDailyBalance方法来验证每日余额曲线图功能。该测试会：
1. 调用reportDailyBalance方法获取数据
2. 验证返回结果不为空
3. 打印日期和金额的表格格式数据
4. 开发者可以通过控制台输出验证数据是否符合预期

## 20250801新增导入账单功能
前端调用api/flow/import实现将excle表中的数据导入到数据库中
实现 /api/flow/import 功能，包括在 BalanceFlowController 中添加API端点和在 BalanceFlowService 中实现Excel数据导入逻辑。
前端可以通过调用此API将Excel表中的数据导入到数据库

## 20250802修改账户的每日变动接口原来实现的是每天的变动情况而不是资金的累计情况

## 导入账单的逻辑不够完善


已修改了importFromExcel方法，使其在导入Excel数据时能够正确处理多表操作，包括分类关系、标签关系和账户余额更新。以下是主要变更点：

1. 新增分类关系处理：如果账单类型为支出或收入，解析Excel中的分类信息并调用categoryRelationService.addRelation方法。

2. 新增标签关系处理：解析Excel中的标签信息并调用tagRelationService.addRelation方法。

3. 账户余额更新：如果账单已确认，调用confirmBalance方法更新账户余额。

4. 新增辅助方法：添加了parseCategoryRelations和parseTags方法，用于解析Excel中的分类和标签数据。

这些修改确保了importFromExcel方法与add方法的逻辑一致，支持多表操作

### 新增支出
2025-08-03T10:56:42.013413Z	 3904 Query	SET autocommit=0
2025-08-03T10:56:42.017152Z	 3904 Query	select b1_0.id,b1_0.default_currency_code,dea1_0.id,dea1_0.apr,dea1_0.balance,dea1_0.bill_day,dea1_0.can_expense,dea1_0.can_income,dea1_0.can_transfer_from,dea1_0.can_transfer_to,dea1_0.credit_limit,dea1_0.currency_code,dea1_0.enable,dea1_0.group_id,dea1_0.include,dea1_0.initial_balance,dea1_0.name,dea1_0.no,dea1_0.notes,dea1_0.ranking,dea1_0.type,dec1_0.id,dec1_0.book_id,dec1_0.enable,dec1_0.level,dec1_0.name,dec1_0.notes,dec1_0.parent_id,dec1_0.ranking,dec1_0.type,dia1_0.id,dia1_0.apr,dia1_0.balance,dia1_0.bill_day,dia1_0.can_expense,dia1_0.can_income,dia1_0.can_transfer_from,dia1_0.can_transfer_to,dia1_0.credit_limit,dia1_0.currency_code,dia1_0.enable,dia1_0.group_id,dia1_0.include,dia1_0.initial_balance,dia1_0.name,dia1_0.no,dia1_0.notes,dia1_0.ranking,dia1_0.type,dic1_0.id,dic1_0.book_id,dic1_0.enable,dic1_0.level,dic1_0.name,dic1_0.notes,dic1_0.parent_id,dic1_0.ranking,dic1_0.type,dtfa1_0.id,dtfa1_0.apr,dtfa1_0.balance,dtfa1_0.bill_day,dtfa1_0.can_expense,dtfa1_0.can_income,dtfa1_0.can_transfer_from,dtfa1_0.can_transfer_to,dtfa1_0.credit_limit,dtfa1_0.currency_code,dtfa1_0.enable,dtfa1_0.group_id,dtfa1_0.include,dtfa1_0.initial_balance,dtfa1_0.name,dtfa1_0.no,dtfa1_0.notes,dtfa1_0.ranking,dtfa1_0.type,dtta1_0.id,dtta1_0.apr,dtta1_0.balance,dtta1_0.bill_day,dtta1_0.can_expense,dtta1_0.can_income,dtta1_0.can_transfer_from,dtta1_0.can_transfer_to,dtta1_0.credit_limit,dtta1_0.currency_code,dtta1_0.enable,dtta1_0.group_id,dtta1_0.include,dtta1_0.initial_balance,dtta1_0.name,dtta1_0.no,dtta1_0.notes,dtta1_0.ranking,dtta1_0.type,b1_0.enable,b1_0.export_at,b1_0.group_id,b1_0.name,b1_0.notes,b1_0.ranking from t_user_book b1_0 left join t_user_account dea1_0 on dea1_0.id=b1_0.default_expense_account_id left join t_user_category dec1_0 on dec1_0.id=b1_0.default_expense_category_id left join t_user_account dia1_0 on dia1_0.id=b1_0.default_income_account_id left join t_user_category dic1_0 on dic1_0.id=b1_0.default_income_category_id left join t_user_account dtfa1_0 on dtfa1_0.id=b1_0.default_transfer_from_account_id left join t_user_account dtta1_0 on dtta1_0.id=b1_0.default_transfer_to_account_id where b1_0.id=3
2025-08-03T10:56:42.019589Z	 3904 Query	select a1_0.id,a1_0.apr,a1_0.balance,a1_0.bill_day,a1_0.can_expense,a1_0.can_income,a1_0.can_transfer_from,a1_0.can_transfer_to,a1_0.credit_limit,a1_0.currency_code,a1_0.enable,a1_0.group_id,a1_0.include,a1_0.initial_balance,a1_0.name,a1_0.no,a1_0.notes,a1_0.ranking,a1_0.type from t_user_account a1_0 where a1_0.id=4
2025-08-03T10:56:42.023262Z	 3904 Query	select c1_0.id,c1_0.book_id,c1_0.enable,c1_0.level,c1_0.name,c1_0.notes,c1_0.parent_id,c1_0.ranking,c1_0.type from t_user_category c1_0 where c1_0.book_id=3 and c1_0.id=17
2025-08-03T10:56:42.024971Z	 3904 Query	select t1_0.id,t1_0.book_id,t1_0.can_expense,t1_0.can_income,t1_0.can_transfer,t1_0.enable,t1_0.level,t1_0.name,t1_0.notes,t1_0.parent_id,t1_0.ranking from t_user_tag t1_0 where t1_0.book_id=3 and t1_0.id=62
2025-08-03T10:56:42.026540Z	 3904 Query	select p1_0.id,p1_0.book_id,p1_0.can_expense,p1_0.can_income,p1_0.enable,p1_0.name,p1_0.notes,p1_0.ranking from t_user_payee p1_0 where p1_0.book_id=3 and p1_0.id=6
2025-08-03T10:56:42.028076Z	 3904 Query	insert into t_user_balance_flow (account_id,amount,book_id,confirm,converted_amount,create_time,creator_id,group_id,include,insert_at,notes,payee_id,title,to_id,type) values (4,20,3,1,20,1754218546154,2,2,1,1754218602021,'新增支出逻辑测试',6,'新增支出逻辑测试',null,100)
2025-08-03T10:56:42.029515Z	 3904 Query	insert into t_user_category_relation (amount,balance_flow_id,category_id,converted_amount) values (20,324,17,20)
2025-08-03T10:56:42.030387Z	 3904 Query	insert into t_user_tag_relation (amount,balance_flow_id,converted_amount,tag_id) values (20,324,20,62)
2025-08-03T10:56:42.032844Z	 3904 Query	SELECT @@session.transaction_read_only
2025-08-03T10:56:42.034248Z	 3904 Query	update t_user_account set apr=null,balance=1311.94,bill_day=null,can_expense=1,can_income=1,can_transfer_from=1,can_transfer_to=1,credit_limit=null,currency_code='CNY',enable=1,group_id=2,include=1,initial_balance=2181.94,name='建设银行',no=null,notes='建设银行账户',ranking=null,type=100 where id=4
2025-08-03T10:56:42.035509Z	 3904 Query	commit