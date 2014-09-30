elasticsearch-sql
=================

 Elasticsearch SQL 查询插件

 支持类SQL的SELECT 查询语句，编译后可以直接放在Elasticsearch的plugins目录。

 采用ANtlr3解析SQL，翻译成Elasticsearch的查询结构，在Elasticsearch1.3.0下编译。

### search RESTfull
    http://localhost:9200/_sql?sql=select * from * limit 10

### SQL explain RESTfull
    http://localhost:9200/_sql/_explain?sql=select * from * limit 10


### SEARCH SYNTAX
=================
 selectClause 
    fromClause 
    whereClause? 
    limitClause? 
        groupClause? 
        facetClause* 
        orderClause?


 主要功能
=================
1. 支持标准SELECT 语句
2. 支持指定多个Elasticsearch的index和mapping type结构
3. SELECT 列表支持函数表达式script('script section')
4. WHERE 支持函数表达式script('script section')
5. 支持group by;同时group by 也支持函数表达式script('script section')
6. 支持Elasticsearch Aggs统计功能。目前实现了date histogram和TopHitsFacet
7. 支持order by
8. 支持count、max、min、avg、sum统计函数

Date Histogram
=================
语法结构: date_his 时间字段 统计间隔 AS:
select count(*) from * date_his @timestamp 1d   -> 按天统计日志数量

Top Hits 
=================
语法结构: top_hits 条数 SORT BY 字段列表 INCLUDE 返回字段列表(*表示全部):
select count(*) from * group by @team  top_hits 1 sort by @timestamp asc include * -->按@team分组，返回每组按@timestamp字段排序后的记录。


 例句
=================
1. select * from *  
2. select count(*) from logstash-2014-10-01
3. select max(script('Integer.parseInt(doc["val"].value)')),min(price),avg(num),count(*) from logstash-2014-10-01
4. select * from * where count>1 limit 0,100
5. select avg(parno),sum(val),script('doc["parno"].value') from logstash-2013-12-22 where @type!='bp' and team like '.*' and parno<=50 and script('doc["parno"].value')>0 and val>=0 and manager in ('a','b') and team not in ("bj1") and not parno<20 limit 10,20 group by obj,team date_his @timestamp 1m top_hits 1 sort by @timestamp asc,parno desc include @a,@b,@c order by team DESC
6. select avg(parno),sum(val),script('doc["parno"].value') from "logstash*" where @type!='bp' and team like '.*' and parno<=50 and script('doc["parno"].value')>0 and val>=0 and @type in (select * from * group by type) and @type not in (select @type from * group by type) and not parno<20 limit 10,20 group by obj,team date_his @timestamp 1M order by team DESC
7. select max(parno),min(tttt),count() from logstash-2014-05-26 group by type
8. select max(aaa),min(bbb),avg(ccc),max(ddd),sum(aaa) from cindex
9. select count(*) from logstash-2013-12-22 group by obj,team
10.select sum(script('Integer.parseInt(doc["parno"].value)*0.1')) AS Legend ,avg(script('Integer.parseInt(doc["parno"].value)*0.1')) AS Legend_avg from *
11.select count(*) from * group by obj
12.select count(*) from * group by script('doc["obj"].value') order by script('doc["obj"].value')
13.select max(log.parno),min(log.tttt),count() from logstash-2014-05-26 group by log.type
14.select * from * where @timestamp>="2014-01-28T00:00:00.000Z"
15.select sum(num) from * date_his @timestamp 1d
16.select count(*) from "logstash*" group by @team  top_hits 1 sort by @timestamp asc include *

期望功能
=================
1. 支持子查询
2. 增加常用函数，优化script结构。
3. 全面支持其他Aggregations 功能。

联系
=================
e-mail:dada9407@163.com
e-mail:chuanyi.chen@gmail.com
QQ:303940973
