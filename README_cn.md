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

测试一下
