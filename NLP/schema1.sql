-- MySQL dump 10.13  Distrib 5.1.49, for debian-linux-gnu (i686)
--
-- Host: localhost    Database: TwitterDB
-- ------------------------------------------------------
-- Server version	5.1.49-1ubuntu8.1

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
-- Table structure for table `RT`
--

DROP TABLE IF EXISTS `RT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RT` (
  `RT_id` int(11) NOT NULL,
  `RT_depth` smallint(5) unsigned NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`RT_id`,`RT_depth`),
  KEY `fk_RT_user1` (`user_id`),
  KEY `fk_RT_tweet1` (`RT_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cityinfo`
--

DROP TABLE IF EXISTS `cityinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cityinfo` (
  `id` int(11) NOT NULL,
  `instance` tinyint(4) NOT NULL,
  `FullName` varchar(256) DEFAULT NULL,
  `PrunedName` varchar(256) DEFAULT NULL,
  `Error` tinyint(4) DEFAULT NULL,
  `Found` tinyint(4) DEFAULT NULL,
  `GlobalQuality` tinyint(4) DEFAULT NULL,
  `Lat` varchar(45) DEFAULT NULL,
  `Lng` varchar(45) DEFAULT NULL,
  `oLat` varchar(45) DEFAULT NULL,
  `olng` varchar(45) DEFAULT NULL,
  `Radius` int(11) DEFAULT NULL,
  `WoeId` int(11) DEFAULT NULL,
  `WoeType` tinyint(4) DEFAULT NULL,
  `TimeZone` varchar(45) DEFAULT NULL,
  `IndivQuality` tinyint(4) DEFAULT NULL,
  `City` varchar(45) DEFAULT NULL,
  `State` varchar(45) DEFAULT NULL,
  `Country` varchar(45) DEFAULT NULL,
  `Postal` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`,`instance`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hashtag`
--

DROP TABLE IF EXISTS `hashtag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hashtag` (
  `id` int(11) NOT NULL,
  `tag` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `topic`
--

DROP TABLE IF EXISTS `topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `topic` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `class` char(1) DEFAULT NULL,
  `entityType` varchar(128) DEFAULT NULL,
  `entityFullName` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tweet`
--

DROP TABLE IF EXISTS `tweet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tweet` (
  `id` int(11) NOT NULL,
  `datetime` datetime DEFAULT NULL,
  `msg` varchar(255) DEFAULT NULL,
  `timestamp` double(16,6) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `total_RT_depth` smallint(5) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`,`user_id`),
  KEY `fk_tweet_user1` (`user_id`),
  CONSTRAINT `fk_tweet_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tweet_has_hashtag`
--

DROP TABLE IF EXISTS `tweet_has_hashtag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tweet_has_hashtag` (
  `tweet_id` int(11) NOT NULL,
  `tweet_user_id` int(11) NOT NULL,
  `hashtag_id` int(11) NOT NULL,
  PRIMARY KEY (`tweet_id`,`tweet_user_id`,`hashtag_id`),
  KEY `fk_tweet_has_hashtag_hashtag1` (`hashtag_id`),
  KEY `fk_tweet_has_hashtag_tweet1` (`tweet_id`,`tweet_user_id`),
  CONSTRAINT `fk_tweet_has_hashtag_hashtag1` FOREIGN KEY (`hashtag_id`) REFERENCES `hashtag` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_tweet_has_hashtag_tweet1` FOREIGN KEY (`tweet_id`, `tweet_user_id`) REFERENCES `tweet` (`id`, `user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tweet_has_topic`
--

DROP TABLE IF EXISTS `tweet_has_topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tweet_has_topic` (
  `tweet_id` int(11) NOT NULL,
  `tweet_user_id` int(11) NOT NULL,
  `topic_id` int(11) NOT NULL,
  `value` double DEFAULT NULL,
  PRIMARY KEY (`tweet_id`,`tweet_user_id`,`topic_id`),
  KEY `fk_tweet_has_topic_topic1` (`topic_id`),
  KEY `fk_tweet_has_topic_tweet1` (`tweet_id`,`tweet_user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tweet_has_url`
--

DROP TABLE IF EXISTS `tweet_has_url`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tweet_has_url` (
  `tweet_id` int(11) NOT NULL,
  `tweet_user_id` int(11) NOT NULL,
  `url_id` int(11) NOT NULL,
  PRIMARY KEY (`tweet_id`,`tweet_user_id`,`url_id`),
  KEY `fk_tweet_has_url_url1` (`url_id`),
  KEY `fk_tweet_has_url_tweet1` (`tweet_id`,`tweet_user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `url`
--

DROP TABLE IF EXISTS `url`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `url` (
  `id` int(11) NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `short_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `twitter_id` varchar(255) DEFAULT NULL,
  `fullName` varchar(255) DEFAULT NULL,
  `twitter_name` varchar(255) DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `friends_count` int(11) DEFAULT NULL,
  `followers_count` int(11) DEFAULT NULL,
  `favourites_count` int(11) DEFAULT NULL,
  `listed_count` int(11) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `timeZone` varchar(255) DEFAULT NULL,
  `utcOffset` tinyint(4) DEFAULT NULL,
  `rateLimitStatus` smallint(6) DEFAULT NULL,
  `isProtected` tinyint(1) DEFAULT NULL,
  `isVerified` tinyint(1) DEFAULT NULL,
  `isExist` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_has_direct_tweet`
--

DROP TABLE IF EXISTS `user_has_direct_tweet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_has_direct_tweet` (
  `sender_id` int(11) NOT NULL,
  `receiver_id` int(11) NOT NULL,
  `tweet_id` int(11) NOT NULL,
  PRIMARY KEY (`tweet_id`),
  KEY `fk_user_has_user_user6` (`receiver_id`),
  KEY `fk_user_has_user_user5` (`sender_id`),
  KEY `fk_user_has_direct_tweet_tweet1` (`tweet_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_has_followers`
--

DROP TABLE IF EXISTS `user_has_followers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_has_followers` (
  `user_id` int(11) NOT NULL,
  `follower_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`follower_id`),
  KEY `fk_user_has_user_user2` (`follower_id`),
  KEY `fk_user_has_user_user1` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_is_following`
--

DROP TABLE IF EXISTS `user_is_following`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_is_following` (
  `user_id` int(11) NOT NULL,
  `following_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`following_id`),
  KEY `fk_user_has_user_user4` (`following_id`),
  KEY `fk_user_has_user_user3` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `userlocation`
--

DROP TABLE IF EXISTS `userlocation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `userlocation` (
  `id` int(11) NOT NULL,
  `cityId` int(11) DEFAULT NULL,
  `LAT` double DEFAULT NULL,
  `LAN` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-08-01  2:51:57
