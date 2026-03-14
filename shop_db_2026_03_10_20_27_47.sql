-- MySQL dump 10.13  Distrib 5.7.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: shop_db
-- ------------------------------------------------------
-- Server version	5.7.44-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `address` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `address_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES (1,'王五','北京市朝阳区建国路88号','13900139003',3),(2,'赵六','上海市浦东新区张江高科技园区','13900139004',4);
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'admin','0192023a7bbd73250516f069df18b500','系统管理员1号','admin_avatar.jpg','admin','13800138000','admin@example.com');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `goods_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'电子产品',NULL,NULL),(2,'生活',NULL,NULL),(3,'服装',NULL,NULL),(4,'美食',NULL,NULL),(5,'二手物品',NULL,NULL);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `collect`
--

DROP TABLE IF EXISTS `collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `collect` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `goods_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collect`
--

LOCK TABLES `collect` WRITE;
/*!40000 ALTER TABLE `collect` DISABLE KEYS */;
INSERT INTO `collect` VALUES (1,25,1),(2,38,1),(3,26,1),(4,19,1),(5,39,1);
/*!40000 ALTER TABLE `collect` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `goods_id` int(11) NOT NULL COMMENT '商品id',
  `content` text NOT NULL COMMENT '评论内容',
  `time` varchar(255) DEFAULT NULL COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `goods_id` (`goods_id`),
  CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `comment_ibfk_2` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `goods`
--

DROP TABLE IF EXISTS `goods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `goods` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `name` varchar(255) NOT NULL COMMENT '商品名称',
  `price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `content` text NOT NULL COMMENT '商品详情',
  `address` varchar(255) NOT NULL COMMENT '发货地址',
  `img` varchar(255) NOT NULL COMMENT '图片路径',
  `date` varchar(255) NOT NULL COMMENT '上架日期',
  `status` varchar(255) NOT NULL COMMENT '审核状态（待审核/审核通过/审核驳回）',
  `category` varchar(255) NOT NULL COMMENT '商品分类',
  `user_id` int(11) NOT NULL COMMENT '所属用户id',
  `sale_status` varchar(255) NOT NULL COMMENT '上架状态（已上架/未上架）',
  `read_count` int(11) NOT NULL DEFAULT '0' COMMENT '浏览量',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `goods_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COMMENT='商品信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `goods`
--

LOCK TABLES `goods` WRITE;
/*!40000 ALTER TABLE `goods` DISABLE KEYS */;
INSERT INTO `goods` VALUES (19,'三星Galaxy S25 Ultra',8999.00,'三星Galaxy S25 Ultra 512GB 幻影黑 骁龙8 Gen4 2亿像素','广东省深圳市','/images/galaxys25.jpg','2025-07-12','审核通过','电子产品',1,'已上架',1092),(20,'索尼A7C III相机',13499.00,'索尼A7C III全画幅微单相机 2420万像素 视频拍照神器','上海市','/images/a7c3.jpg','2025-07-08','审核通过','电子产品',1,'已上架',765),(21,'大疆Air 4S无人机',7999.00,'大疆Air 4S航拍无人机 4K超清 智能避障 长续航','广东省广州市','/images/dji-air4s.jpg','2025-07-03','审核通过','电子产品',1,'已上架',874),(22,'任天堂Switch OLED',2099.00,'任天堂Switch OLED版 掌上游戏机 续航增强 白色','北京市','/images/switch.jpg','2025-06-30 00:00:00','审核通过','电子产品',1,'已上架',1230),(23,'Bose QuietComfort Ultra耳机',2799.00,'Bose QuietComfort Ultra 无线降噪耳机 主动降噪 通话清晰','浙江省杭州市','/images/bose-headphones.jpg','2025-07-15','审核通过','电子产品',1,'已上架',921),(24,'阳澄湖大闸蟹礼盒',399.00,'阳澄湖大闸蟹8只装 公4两母3两 鲜活直达 礼券兑换','江苏省苏州市','/images/crab.jpg','2025-07-18','审核通过','美食',1,'已上架',643),(25,'云南普洱茶饼',599.00,'云南古树普洱茶饼 357g 2020年陈化 越陈越香','云南省昆明市','/images/tea.jpg','2025-07-16','审核通过','美食',1,'已上架',456),(26,'进口车厘子5斤装',258.00,'智利进口车厘子 5斤装 新鲜直达 圆润饱满','上海市','/images/cherry.jpg','2025-07-14','审核通过','美食',1,'已上架',789),(27,'手工巧克力礼盒',198.00,'比利时进口巧克力礼盒 20粒装 多种口味 送礼佳品','北京市','/images/chocolate.jpg','2025-07-11','审核通过','美食',1,'已上架',532),(28,'五常稻花香大米5kg',128.00,'黑龙江五常稻花香大米 5kg 真空包装 软糯香甜','黑龙江省哈尔滨市','/images/rice.jpg','2025-07-09','审核通过','美食',1,'已上架',678),(29,'李宁男子跑步鞋',599.00,'李宁男子跑步鞋 减震防滑 透气网面 运动健身必备','福建省泉州市','/images/li-ning-shoes.jpg','2025-07-20','审核通过','服装',1,'已上架',876),(30,'波司登轻薄羽绒服',899.00,'波司登男士轻薄羽绒服 90白鸭绒 保暖轻便 修身款','上海市','/images/bosideng.jpg','2025-07-17','审核通过','服装',1,'已上架',765),(31,'优衣库纯棉T恤',79.00,'优衣库男士纯棉T恤 多色可选 宽松版型 舒适透气','上海市','/images/uniqlo-tshirt.jpg','2025-07-13','审核通过','服装',1,'已上架',1234),(32,'ZARA春季风衣',799.00,'ZARA男士春季风衣 百搭款 修身剪裁 高品质面料','广东省广州市','/images/zara-coat.jpg','2025-07-10','审核通过','服装',1,'已上架',987),(33,'南极人纯棉睡衣套装',159.00,'南极人男士纯棉睡衣套装 舒适亲肤 多色可选','江苏省南京市','/images/nanjiren-pajamas.jpg','2025-07-07','审核通过','服装',1,'已上架',654),(34,'小米空气净化器4 Pro',1499.00,'小米空气净化器4 Pro 除甲醛PM2.5 智能控制 静音设计','北京市','/images/air-purifier.jpg','2025-07-22','审核通过','生活',1,'已上架',876),(35,'科沃斯扫地机器人T20',3499.00,'科沃斯T20扫地机器人 扫拖一体 自动集尘 避障灵敏','江苏省苏州市','/images/robot-vacuum.jpg','2025-07-21','审核通过','生活',1,'已上架',765),(36,'网易严选床垫',1899.00,'网易严选弹簧床垫 1.8米 独立袋装弹簧 透气护脊','浙江省杭州市','/images/mattress.jpg','2025-07-19','审核通过','生活',1,'已上架',987),(37,'膳魔师保温杯',299.00,'膳魔师保温杯500ml 不锈钢真空保温 长效锁温 多色可选','上海市','/images/thermos.jpg','2025-07-16','审核通过','生活',1,'已上架',654),(38,'宜家家居沙发',3999.00,'宜家双人沙发 北欧风格 可拆洗面料 舒适坐感','上海市','/images/ikea-sofa.jpg','2025-07-12','审核通过','生活',1,'已上架',543),(39,'二手联想笔记本电脑',2499.00,'联想小新Pro14 2024款 酷睿i5 16GB 512GB 9成新','北京市','/images/lenovo-laptop.jpg','2025-07-23','审核通过','二手物品',1,'已上架',432),(40,'二手佳能单反相机',3299.00,'佳能EOS 90D 单反相机 套机 18-135mm镜头 95成新','广东省深圳市','/images/canon-camera.jpg','2025-07-22','审核通过','二手物品',1,'已上架',567),(41,'二手电动自行车',1299.00,'小牛电动NQi GT 锂电池 续航80公里 9成新 个人转让','上海市','/images/electric-bike.jpg','2025-07-20','审核通过','二手物品',1,'已上架',321),(42,'二手婴儿床',399.00,'好孩子婴儿床 可折叠 带蚊帐 实木材质 9.5成新','江苏省南京市','/images/baby-crib.jpg','2025-07-18','审核通过','二手物品',1,'已上架',234),(43,'二手跑步机',1599.00,'亿健家用跑步机 减震降噪 多功能 8成新 带遥控器','浙江省杭州市','/images/treadmill.jpg','2025-07-15','审核通过','二手物品',1,'已上架',678),(44,'222',333.00,'444','','','2025-07-24 19:05:59.67','审核通过','二手物品',12,'已上架',0),(45,'333',444.00,'333','','','2025-07-25 10:31:03.128','未审核','二手物品',12,'未上架',0);
/*!40000 ALTER TABLE `goods` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notice`
--

DROP TABLE IF EXISTS `notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notice` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `time` datetime NOT NULL,
  `user` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notice`
--

LOCK TABLES `notice` WRITE;
/*!40000 ALTER TABLE `notice` DISABLE KEYS */;
INSERT INTO `notice` VALUES (2,'促销活动','春季大促销即将开始','2025-07-20 19:45:09','admin'),(4,'111','222','2025-07-24 01:02:07','admin'),(5,'333','444','2025-07-24 01:02:11','admin'),(6,'清仓大甩卖','全场1折','2025-07-24 01:02:31','admin'),(7,'222','333','2025-07-24 01:02:35','admin'),(8,'夏季促销活动开启','全场商品满200减50，限时一周，欢迎选购！','2025-07-20 10:00:00','admin'),(9,'新品上架通知','全新款式服装、数码产品已上架，快来挑选心仪好物','2025-07-21 10:10:00','admin');
/*!40000 ALTER TABLE `notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `orders` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `goods_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `goods_img` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `time` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pay_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `pay_time` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sale_id` int(11) NOT NULL,
  `goods_price` decimal(10,2) DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `order_no` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (6,'宜家家居沙发','/images/ikea-sofa.jpg',3999.00,'2025-07-24 20:15:38','PAY1753359337872','2025-07-24 12:15:37','111','18611112222','222','已发货',1001,3999.00,1,'ORD1753359338194',1),(7,'阳澄湖大闸蟹礼盒','/images/crab.jpg',399.00,'2025-07-25 00:01:24','PAY1753372883752','2025-07-24 16:01:23','111','13099998888','22','已支付',1001,399.00,1,'ORD1753372884068',1),(8,'三星Galaxy S25 Ultra','/images/galaxys25.jpg',8999.00,'2025-07-25 13:14:24','PAY1753420464245','2025-07-25 05:14:24','五3','13399998888','222','已支付',1001,8999.00,1,'ORD1753420464554',12);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'default_avatar.jpg',
  `role` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'seller1','111111','张三','seller1_avatar.jpg','seller','13900139001','seller1@example.com'),(2,'seller2','222222','李四','seller2_avatar.jpg','seller','13900139002','seller2@example.com'),(3,'buyer1','333333','王五','buyer1_avatar.jpg','buyer','13900139003','buyer1@example.com'),(4,'buyer2','444444','赵六','buyer2_avatar.jpg','buyer','13900139004','buyer2@example.com'),(5,'test1','000','鸟兽受','default_avatar.jpg','buyer','13211117777',''),(7,'test23','444','钱钱钱','default_avatar.jpg','buyer','12312341234',''),(11,'test3','333','嗡嗡嗡','default_avatar.jpg','seller','12333334444',''),(12,'test4','444444','呃饿','default_avatar.jpg','buyer','3333333466',''),(13,'buyer10','101010','赵十','default_avatar.jpg','user','13444556677','');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-10 20:27:48
