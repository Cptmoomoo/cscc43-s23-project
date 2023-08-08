CREATE DATABASE  IF NOT EXISTS `mydb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `mydb`;
-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: mydb
-- ------------------------------------------------------
-- Server version	8.1.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `amenities`
--

DROP TABLE IF EXISTS `amenities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `amenities` (
  `Name` varchar(100) NOT NULL,
  `Listing_id` char(36) NOT NULL,
  PRIMARY KEY (`Name`,`Listing_id`),
  KEY `Listing_id` (`Listing_id`),
  CONSTRAINT `amenities_ibfk_1` FOREIGN KEY (`Listing_id`) REFERENCES `listings` (`Listing_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `amenities`
--

LOCK TABLES `amenities` WRITE;
/*!40000 ALTER TABLE `amenities` DISABLE KEYS */;
INSERT INTO `amenities` VALUES ('KITCHEN','05e2941b-3ed2-418b-95b1-b2ee3e0d25d7'),('PARKING','05e2941b-3ed2-418b-95b1-b2ee3e0d25d7'),('POOL','05e2941b-3ed2-418b-95b1-b2ee3e0d25d7'),('WIFI','05e2941b-3ed2-418b-95b1-b2ee3e0d25d7'),('ACCESSIBLE_BATHROOM','09966a03-c0a9-4a68-9823-fdf87a8b3c77'),('AIR_CONDITIONING','09966a03-c0a9-4a68-9823-fdf87a8b3c77'),('HEATER','09966a03-c0a9-4a68-9823-fdf87a8b3c77'),('WIDE_ENTRANCE','09966a03-c0a9-4a68-9823-fdf87a8b3c77'),('WIDE_HALLWAYS','09966a03-c0a9-4a68-9823-fdf87a8b3c77'),('AIR_CONDITIONING','13f563a9-c3a5-450b-a651-82a81e5d1c60'),('HEATER','13f563a9-c3a5-450b-a651-82a81e5d1c60'),('JACUZZI','13f563a9-c3a5-450b-a651-82a81e5d1c60'),('KITCHEN','13f563a9-c3a5-450b-a651-82a81e5d1c60'),('PARKING','13f563a9-c3a5-450b-a651-82a81e5d1c60'),('POOL','13f563a9-c3a5-450b-a651-82a81e5d1c60'),('WIFI','13f563a9-c3a5-450b-a651-82a81e5d1c60'),('AIR_CONDITIONING','3993dd80-b9b3-4a0b-a067-682410339021'),('HEATER','3993dd80-b9b3-4a0b-a067-682410339021'),('WIDE_ENTRANCE','3993dd80-b9b3-4a0b-a067-682410339021'),('WIDE_HALLWAYS','3993dd80-b9b3-4a0b-a067-682410339021'),('BREAKFAST','3d9ff0b7-f916-4ede-97d6-5020d43d093e'),('HEATER','3d9ff0b7-f916-4ede-97d6-5020d43d093e'),('WIFI','3d9ff0b7-f916-4ede-97d6-5020d43d093e'),('AIR_CONDITIONING','553979ee-8b1a-4360-b99d-e48579f92397'),('HEATER','553979ee-8b1a-4360-b99d-e48579f92397'),('WIFI','553979ee-8b1a-4360-b99d-e48579f92397'),('KITCHEN','5a26064d-76e1-419b-9d60-fa4bb1fd6ec0'),('PARKING','5a26064d-76e1-419b-9d60-fa4bb1fd6ec0'),('POOL','5a26064d-76e1-419b-9d60-fa4bb1fd6ec0'),('WIFI','5a26064d-76e1-419b-9d60-fa4bb1fd6ec0'),('ACCESSIBLE_BATHROOM','7a971975-b9d4-44cb-9975-8d0fabc059b9'),('AIR_CONDITIONING','7a971975-b9d4-44cb-9975-8d0fabc059b9'),('BREAKFAST','7a971975-b9d4-44cb-9975-8d0fabc059b9'),('HEATER','7a971975-b9d4-44cb-9975-8d0fabc059b9'),('PARKING','7a971975-b9d4-44cb-9975-8d0fabc059b9'),('POOL','7a971975-b9d4-44cb-9975-8d0fabc059b9'),('STEP_FREE_ENTRANCE','7a971975-b9d4-44cb-9975-8d0fabc059b9'),('WIDE_ENTRANCE','7a971975-b9d4-44cb-9975-8d0fabc059b9'),('WIDE_HALLWAYS','7a971975-b9d4-44cb-9975-8d0fabc059b9'),('WIFI','7a971975-b9d4-44cb-9975-8d0fabc059b9'),('POOL','a46dc173-993e-4283-9a9a-c99f66f812d8'),('WIFI','a46dc173-993e-4283-9a9a-c99f66f812d8'),('POOL','a6625ac8-f536-4be4-8b1d-e75af1ab8ee6'),('WIFI','a6625ac8-f536-4be4-8b1d-e75af1ab8ee6'),('AIR_CONDITIONING','c115a426-8505-4e2d-9ac8-ffb0ff196ad0'),('HEATER','c115a426-8505-4e2d-9ac8-ffb0ff196ad0'),('JACUZZI','c115a426-8505-4e2d-9ac8-ffb0ff196ad0'),('KITCHEN','c115a426-8505-4e2d-9ac8-ffb0ff196ad0'),('PARKING','c115a426-8505-4e2d-9ac8-ffb0ff196ad0'),('POOL','c115a426-8505-4e2d-9ac8-ffb0ff196ad0'),('WIFI','c115a426-8505-4e2d-9ac8-ffb0ff196ad0'),('BREAKFAST','cf0e6c63-09dc-4a40-b11d-7215a1ddccf2'),('KITCHEN','cf0e6c63-09dc-4a40-b11d-7215a1ddccf2'),('POOL','cf0e6c63-09dc-4a40-b11d-7215a1ddccf2'),('WIFI','cf0e6c63-09dc-4a40-b11d-7215a1ddccf2'),('POOL','f3eb193a-d521-4201-9796-6eec107941de');
/*!40000 ALTER TABLE `amenities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `availability`
--

DROP TABLE IF EXISTS `availability`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `availability` (
  `Start_date` date NOT NULL,
  `End_date` date DEFAULT NULL,
  `Listing_id` char(36) NOT NULL,
  `Price_per_day` float DEFAULT NULL,
  PRIMARY KEY (`Start_date`,`Listing_id`),
  KEY `Listing_id` (`Listing_id`),
  CONSTRAINT `availability_ibfk_1` FOREIGN KEY (`Listing_id`) REFERENCES `listings` (`Listing_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `availability`
--

LOCK TABLES `availability` WRITE;
/*!40000 ALTER TABLE `availability` DISABLE KEYS */;
INSERT INTO `availability` VALUES ('2023-01-01','2023-12-31','05e2941b-3ed2-418b-95b1-b2ee3e0d25d7',1062.5),('2023-01-01','2023-12-31','09966a03-c0a9-4a68-9823-fdf87a8b3c77',555),('2023-01-01','2023-12-31','13f563a9-c3a5-450b-a651-82a81e5d1c60',221.25),('2023-01-01','2023-12-31','3993dd80-b9b3-4a0b-a067-682410339021',556.25),('2023-01-01','2023-12-31','3d9ff0b7-f916-4ede-97d6-5020d43d093e',543.75),('2023-01-01','2023-04-03','553979ee-8b1a-4360-b99d-e48579f92397',69.5),('2023-01-01','2023-12-31','5a26064d-76e1-419b-9d60-fa4bb1fd6ec0',450),('2023-01-01','2023-12-31','7a971975-b9d4-44cb-9975-8d0fabc059b9',505),('2023-01-01','2023-04-03','a46dc173-993e-4283-9a9a-c99f66f812d8',537.5),('2023-01-01','2023-12-31','a6625ac8-f536-4be4-8b1d-e75af1ab8ee6',172.5),('2023-01-01','2023-03-02','cf0e6c63-09dc-4a40-b11d-7215a1ddccf2',77.5),('2023-01-01','2023-03-02','f3eb193a-d521-4201-9796-6eec107941de',393.75),('2023-03-18','2023-12-31','f3eb193a-d521-4201-9796-6eec107941de',393.75),('2023-04-05','2023-12-31','cf0e6c63-09dc-4a40-b11d-7215a1ddccf2',77.5),('2023-04-07','2023-12-31','553979ee-8b1a-4360-b99d-e48579f92397',69.5),('2023-04-14','2023-12-31','a46dc173-993e-4283-9a9a-c99f66f812d8',537.5),('2024-01-01','2024-04-30','13f563a9-c3a5-450b-a651-82a81e5d1c60',221.25),('2024-01-01','2024-12-31','5a26064d-76e1-419b-9d60-fa4bb1fd6ec0',450),('2024-01-01','2024-12-31','7a971975-b9d4-44cb-9975-8d0fabc059b9',505),('2024-01-01','2024-02-01','cf0e6c63-09dc-4a40-b11d-7215a1ddccf2',77.5),('2024-02-05','2024-12-31','cf0e6c63-09dc-4a40-b11d-7215a1ddccf2',77.5);
/*!40000 ALTER TABLE `availability` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `belongs_to`
--

DROP TABLE IF EXISTS `belongs_to`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `belongs_to` (
  `Listing_id` char(36) NOT NULL,
  `Longitude` float DEFAULT NULL,
  `Latitude` float DEFAULT NULL,
  PRIMARY KEY (`Listing_id`),
  KEY `FK_Belongs_to` (`Longitude`,`Latitude`),
  CONSTRAINT `belongs_to_ibfk_1` FOREIGN KEY (`Listing_id`) REFERENCES `listings` (`Listing_id`) ON DELETE CASCADE,
  CONSTRAINT `FK_Belongs_to` FOREIGN KEY (`Longitude`, `Latitude`) REFERENCES `locations` (`Longitude`, `Latitude`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `belongs_to`
--

LOCK TABLES `belongs_to` WRITE;
/*!40000 ALTER TABLE `belongs_to` DISABLE KEYS */;
INSERT INTO `belongs_to` VALUES ('a46dc173-993e-4283-9a9a-c99f66f812d8',1,1),('f3eb193a-d521-4201-9796-6eec107941de',1,1),('09966a03-c0a9-4a68-9823-fdf87a8b3c77',10,10),('05e2941b-3ed2-418b-95b1-b2ee3e0d25d7',32,32),('3d9ff0b7-f916-4ede-97d6-5020d43d093e',32,32),('553979ee-8b1a-4360-b99d-e48579f92397',32,32),('c115a426-8505-4e2d-9ac8-ffb0ff196ad0',32,32),('3993dd80-b9b3-4a0b-a067-682410339021',44,44),('13f563a9-c3a5-450b-a651-82a81e5d1c60',45,45),('5a26064d-76e1-419b-9d60-fa4bb1fd6ec0',67,67),('7a971975-b9d4-44cb-9975-8d0fabc059b9',67,67),('cf0e6c63-09dc-4a40-b11d-7215a1ddccf2',67,67),('a6625ac8-f536-4be4-8b1d-e75af1ab8ee6',88,88);
/*!40000 ALTER TABLE `belongs_to` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `Booking_id` char(36) NOT NULL,
  `Listing_id` char(36) DEFAULT NULL,
  `Start_date` date DEFAULT NULL,
  `End_date` date DEFAULT NULL,
  `Renter_id` char(36) DEFAULT NULL,
  `Total_price` float DEFAULT NULL,
  `Card_number` char(16) DEFAULT NULL,
  `Cancelled_by` char(36) DEFAULT '',
  PRIMARY KEY (`Booking_id`),
  KEY `Listing_id` (`Listing_id`),
  KEY `Renter_id` (`Renter_id`),
  CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`Listing_id`) REFERENCES `listings` (`Listing_id`) ON DELETE CASCADE,
  CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`Renter_id`) REFERENCES `users` (`Username`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
INSERT INTO `bookings` VALUES ('20da0aff-3ccf-481e-b835-3ed12072a8d7','f3eb193a-d521-4201-9796-6eec107941de','2023-03-03','2023-03-17','thing',5512.5,'6666555566665555',''),('25d5a860-8d8f-4ed3-95d6-095c41bdfc43','cf0e6c63-09dc-4a40-b11d-7215a1ddccf2','2023-03-03','2023-04-04','coolguy',2480,'1111222233334444',''),('5df63f44-8633-4a35-b3f8-4d50004178a9','553979ee-8b1a-4360-b99d-e48579f92397','2023-02-02','2023-04-04','thing',4117.5,'6666555566665555','thing'),('72a5fa60-c5ea-4311-ab33-5d1fe52e5641','553979ee-8b1a-4360-b99d-e48579f92397','2023-02-02','2023-02-05','thing',202.5,'6666555566665555','vli'),('a852a617-065e-4444-a9f1-1559cf0cc815','cf0e6c63-09dc-4a40-b11d-7215a1ddccf2','2024-02-02','2024-02-04','coolguy',155,'1111222233334444',''),('d25a4e60-762d-4ee2-ab15-cb64c7bcc09a','a46dc173-993e-4283-9a9a-c99f66f812d8','2023-04-04','2023-04-13','thing',4837.5,'6666555566665555',''),('d72f74bd-264b-45e0-b366-d299f598a6e2','553979ee-8b1a-4360-b99d-e48579f92397','2023-04-04','2023-04-06','thing',139,'6666555566665555',''),('ff5930a4-cf55-482d-8c38-1261cf965a84','553979ee-8b1a-4360-b99d-e48579f92397','2023-01-03','2023-04-04','thing',6142.5,'6666555566665555','thing');
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment_listing`
--

DROP TABLE IF EXISTS `comment_listing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_listing` (
  `Comment_id` char(36) NOT NULL,
  `Username` varchar(100) DEFAULT NULL,
  `Listing_id` char(36) DEFAULT NULL,
  `Text` varchar(255) DEFAULT NULL,
  `Timestamp` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`Comment_id`),
  UNIQUE KEY `Comment_id` (`Comment_id`),
  KEY `Username` (`Username`),
  KEY `Listing_id` (`Listing_id`),
  CONSTRAINT `comment_listing_ibfk_1` FOREIGN KEY (`Username`) REFERENCES `users` (`Username`) ON DELETE CASCADE,
  CONSTRAINT `comment_listing_ibfk_2` FOREIGN KEY (`Listing_id`) REFERENCES `listings` (`Listing_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_listing`
--

LOCK TABLES `comment_listing` WRITE;
/*!40000 ALTER TABLE `comment_listing` DISABLE KEYS */;
INSERT INTO `comment_listing` VALUES ('2d48c776-e860-489a-942a-87da178b1ba4','thing','a46dc173-993e-4283-9a9a-c99f66f812d8','good plave','2023-08-08 11:18:16');
/*!40000 ALTER TABLE `comment_listing` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment_user`
--

DROP TABLE IF EXISTS `comment_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment_user` (
  `Comment_id` char(36) NOT NULL,
  `Reviewer` varchar(100) DEFAULT NULL,
  `Reviewee` varchar(100) DEFAULT NULL,
  `Text` varchar(255) DEFAULT NULL,
  `Timestamp` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`Comment_id`),
  UNIQUE KEY `Comment_id` (`Comment_id`),
  KEY `Reviewer` (`Reviewer`),
  KEY `Reviewee` (`Reviewee`),
  CONSTRAINT `comment_user_ibfk_1` FOREIGN KEY (`Reviewer`) REFERENCES `users` (`Username`) ON DELETE CASCADE,
  CONSTRAINT `comment_user_ibfk_2` FOREIGN KEY (`Reviewee`) REFERENCES `users` (`Username`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment_user`
--

LOCK TABLES `comment_user` WRITE;
/*!40000 ALTER TABLE `comment_user` DISABLE KEYS */;
INSERT INTO `comment_user` VALUES ('16962a75-951f-49f8-8042-8c8e9eab9a28','vli','thing','good guest','2023-08-08 11:18:55'),('53ee5551-03ac-4e52-94fb-f11402dce83a','thing','rock','great','2023-08-08 11:17:52');
/*!40000 ALTER TABLE `comment_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `host_of`
--

DROP TABLE IF EXISTS `host_of`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `host_of` (
  `Username` varchar(100) NOT NULL,
  `Listing_id` char(36) NOT NULL,
  PRIMARY KEY (`Username`,`Listing_id`),
  KEY `Listing_id` (`Listing_id`),
  CONSTRAINT `host_of_ibfk_1` FOREIGN KEY (`Username`) REFERENCES `users` (`Username`) ON DELETE CASCADE,
  CONSTRAINT `host_of_ibfk_2` FOREIGN KEY (`Listing_id`) REFERENCES `listings` (`Listing_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `host_of`
--

LOCK TABLES `host_of` WRITE;
/*!40000 ALTER TABLE `host_of` DISABLE KEYS */;
INSERT INTO `host_of` VALUES ('rock','05e2941b-3ed2-418b-95b1-b2ee3e0d25d7'),('joe','09966a03-c0a9-4a68-9823-fdf87a8b3c77'),('vli','13f563a9-c3a5-450b-a651-82a81e5d1c60'),('vader','3993dd80-b9b3-4a0b-a067-682410339021'),('joe','3d9ff0b7-f916-4ede-97d6-5020d43d093e'),('vli','553979ee-8b1a-4360-b99d-e48579f92397'),('vli','5a26064d-76e1-419b-9d60-fa4bb1fd6ec0'),('vli','7a971975-b9d4-44cb-9975-8d0fabc059b9'),('rock','a46dc173-993e-4283-9a9a-c99f66f812d8'),('rock','a6625ac8-f536-4be4-8b1d-e75af1ab8ee6'),('vli','c115a426-8505-4e2d-9ac8-ffb0ff196ad0'),('joe','cf0e6c63-09dc-4a40-b11d-7215a1ddccf2'),('rock','f3eb193a-d521-4201-9796-6eec107941de');
/*!40000 ALTER TABLE `host_of` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `listings`
--

DROP TABLE IF EXISTS `listings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `listings` (
  `Listing_id` char(36) NOT NULL,
  `Listing_type` varchar(15) DEFAULT NULL,
  `Suite_number` varchar(5) DEFAULT NULL,
  `Max_guests` int DEFAULT NULL,
  `Is_active` tinyint(1) DEFAULT NULL,
  `Time_listed` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`Listing_id`),
  UNIQUE KEY `Listing_id` (`Listing_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `listings`
--

LOCK TABLES `listings` WRITE;
/*!40000 ALTER TABLE `listings` DISABLE KEYS */;
INSERT INTO `listings` VALUES ('05e2941b-3ed2-418b-95b1-b2ee3e0d25d7','ENTIRE_PLACE','',8,1,'2023-08-08 09:30:08'),('09966a03-c0a9-4a68-9823-fdf87a8b3c77','HOTEL_ROOM','32',5,1,'2023-08-08 09:18:49'),('13f563a9-c3a5-450b-a651-82a81e5d1c60','PRIVATE_ROOM','',2,1,'2023-08-08 08:56:04'),('3993dd80-b9b3-4a0b-a067-682410339021','ENTIRE_PLACE','54',4,1,'2023-08-08 09:34:02'),('3d9ff0b7-f916-4ede-97d6-5020d43d093e','ENTIRE_PLACE','32',4,1,'2023-08-08 09:09:28'),('553979ee-8b1a-4360-b99d-e48579f92397','SHARED_ROOM','43',1,1,'2023-08-08 08:59:35'),('5a26064d-76e1-419b-9d60-fa4bb1fd6ec0','HOTEL_ROOM','206',4,1,'2023-08-08 08:58:28'),('7a971975-b9d4-44cb-9975-8d0fabc059b9','HOTEL_ROOM','105',4,1,'2023-08-08 08:57:43'),('a46dc173-993e-4283-9a9a-c99f66f812d8','ENTIRE_PLACE','43',4,1,'2023-08-08 09:23:41'),('a6625ac8-f536-4be4-8b1d-e75af1ab8ee6','PRIVATE_ROOM','32',2,1,'2023-08-08 09:31:05'),('c115a426-8505-4e2d-9ac8-ffb0ff196ad0','ENTIRE_PLACE','1',4,1,'2023-08-08 08:51:15'),('cf0e6c63-09dc-4a40-b11d-7215a1ddccf2','SHARED_ROOM','432',1,1,'2023-08-08 09:21:17'),('f3eb193a-d521-4201-9796-6eec107941de','PRIVATE_ROOM','43',5,1,'2023-08-08 09:28:54');
/*!40000 ALTER TABLE `listings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `locations`
--

DROP TABLE IF EXISTS `locations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `locations` (
  `Longitude` float NOT NULL,
  `Latitude` float NOT NULL,
  `Postal_code` char(6) DEFAULT NULL,
  `Street_num` varchar(100) DEFAULT NULL,
  `Street_name` varchar(10) DEFAULT NULL,
  `City` varchar(100) DEFAULT NULL,
  `Country` varchar(100) DEFAULT NULL,
  `Province` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`Longitude`,`Latitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locations`
--

LOCK TABLES `locations` WRITE;
/*!40000 ALTER TABLE `locations` DISABLE KEYS */;
INSERT INTO `locations` VALUES (1,1,'Y4Y4Y4','54','yes','TORONTO','USA','ONTARIO'),(10,10,'A1A9B9','43','what','TORONTO','CANADA','ONTARIO'),(32,32,'A1A1A1','43','young st','TORONTO','CANADA','ONTARIO'),(44,44,'Y4Y4Y4','88','idk','TORONTO','USA','ONTARIO'),(45,45,'A1A4B4','55','test','TORONTO','CANADA','ONTARIO'),(67,67,'A1A4C4','67','hotel','TORONTO','CANADA','ONTARIO'),(88,88,'T5T5T5','54','too','TORONTO','USA','NA');
/*!40000 ALTER TABLE `locations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paid_with`
--

DROP TABLE IF EXISTS `paid_with`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paid_with` (
  `Username` varchar(100) NOT NULL,
  `Card_number` char(16) NOT NULL,
  PRIMARY KEY (`Username`,`Card_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paid_with`
--

LOCK TABLES `paid_with` WRITE;
/*!40000 ALTER TABLE `paid_with` DISABLE KEYS */;
INSERT INTO `paid_with` VALUES ('coolguy','1111222233334444'),('thing','6666555566665555');
/*!40000 ALTER TABLE `paid_with` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_info`
--

DROP TABLE IF EXISTS `payment_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_info` (
  `Card_number` char(16) NOT NULL,
  `Security_Code` char(3) DEFAULT NULL,
  `Expiration_date` date DEFAULT NULL,
  `First_name` varchar(100) DEFAULT NULL,
  `Last_name` varchar(100) DEFAULT NULL,
  `Postal_code` char(6) DEFAULT NULL,
  PRIMARY KEY (`Card_number`),
  UNIQUE KEY `Card_number` (`Card_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_info`
--

LOCK TABLES `payment_info` WRITE;
/*!40000 ALTER TABLE `payment_info` DISABLE KEYS */;
INSERT INTO `payment_info` VALUES ('1111222233334444','323','2023-02-28','Vincent','Li','A1A1A1'),('6666555566665555','434','2023-04-30','Ben','biden','E3E3E3');
/*!40000 ALTER TABLE `payment_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rate_listing`
--

DROP TABLE IF EXISTS `rate_listing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rate_listing` (
  `Username` varchar(100) NOT NULL,
  `Listing_id` char(36) NOT NULL,
  `Rating` float DEFAULT NULL,
  `Timestamp` timestamp NOT NULL,
  PRIMARY KEY (`Username`,`Listing_id`,`Timestamp`),
  KEY `Listing_id` (`Listing_id`),
  CONSTRAINT `rate_listing_ibfk_1` FOREIGN KEY (`Username`) REFERENCES `users` (`Username`) ON DELETE CASCADE,
  CONSTRAINT `rate_listing_ibfk_2` FOREIGN KEY (`Listing_id`) REFERENCES `listings` (`Listing_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rate_listing`
--

LOCK TABLES `rate_listing` WRITE;
/*!40000 ALTER TABLE `rate_listing` DISABLE KEYS */;
INSERT INTO `rate_listing` VALUES ('thing','a46dc173-993e-4283-9a9a-c99f66f812d8',5,'2023-08-08 11:18:06');
/*!40000 ALTER TABLE `rate_listing` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rate_user`
--

DROP TABLE IF EXISTS `rate_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rate_user` (
  `Reviewer` varchar(100) NOT NULL,
  `Reviewee` varchar(100) NOT NULL,
  `Rating` float DEFAULT NULL,
  `Timestamp` timestamp NOT NULL,
  PRIMARY KEY (`Reviewer`,`Reviewee`,`Timestamp`),
  KEY `Reviewee` (`Reviewee`),
  CONSTRAINT `rate_user_ibfk_1` FOREIGN KEY (`Reviewer`) REFERENCES `users` (`Username`) ON DELETE CASCADE,
  CONSTRAINT `rate_user_ibfk_2` FOREIGN KEY (`Reviewee`) REFERENCES `users` (`Username`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rate_user`
--

LOCK TABLES `rate_user` WRITE;
/*!40000 ALTER TABLE `rate_user` DISABLE KEYS */;
INSERT INTO `rate_user` VALUES ('thing','rock',3,'2023-08-08 11:17:43'),('vli','thing',4,'2023-08-08 11:18:47');
/*!40000 ALTER TABLE `rate_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `Username` varchar(100) NOT NULL,
  `Password` varchar(100) DEFAULT NULL,
  `SIN` char(9) DEFAULT NULL,
  `Occupation` varchar(100) DEFAULT NULL,
  `Date_of_birth` date DEFAULT NULL,
  `First_name` varchar(100) DEFAULT NULL,
  `Last_name` varchar(100) DEFAULT NULL,
  `User_type` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`Username`),
  UNIQUE KEY `Username` (`Username`),
  UNIQUE KEY `SIN` (`SIN`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('coolguy','1ARVn2Auq2/WAqx2gNrL+q3RNjAzXpUfCXrzkA6d4Xa22yhRLy4AC50E+6UTPoscbo31nbOoq51gvkuXzJ6B2w==','555555555','CoolGUy','2000-01-01','Cool','Guy','RENTER'),('joe','1ARVn2Auq2/WAqx2gNrL+q3RNjAzXpUfCXrzkA6d4Xa22yhRLy4AC50E+6UTPoscbo31nbOoq51gvkuXzJ6B2w==','111111111','President','2001-01-01','Joe','Biden','HOST'),('obama','1ARVn2Auq2/WAqx2gNrL+q3RNjAzXpUfCXrzkA6d4Xa22yhRLy4AC50E+6UTPoscbo31nbOoq51gvkuXzJ6B2w==','999999999','President','2000-01-01','Barack','Obama','RENTER'),('rock','1ARVn2Auq2/WAqx2gNrL+q3RNjAzXpUfCXrzkA6d4Xa22yhRLy4AC50E+6UTPoscbo31nbOoq51gvkuXzJ6B2w==','222222222','Rock','2002-01-01','Dwayne','Johnson','HOST'),('sleep','1ARVn2Auq2/WAqx2gNrL+q3RNjAzXpUfCXrzkA6d4Xa22yhRLy4AC50E+6UTPoscbo31nbOoq51gvkuXzJ6B2w==','666666666','Sleep','2000-02-02','I Want','sleep','RENTER'),('thing','1ARVn2Auq2/WAqx2gNrL+q3RNjAzXpUfCXrzkA6d4Xa22yhRLy4AC50E+6UTPoscbo31nbOoq51gvkuXzJ6B2w==','777777777','Thing','2000-01-01','thing','thing','RENTER'),('user','1ARVn2Auq2/WAqx2gNrL+q3RNjAzXpUfCXrzkA6d4Xa22yhRLy4AC50E+6UTPoscbo31nbOoq51gvkuXzJ6B2w==','444444444','User','2002-01-01','first','last','RENTER'),('vader','1ARVn2Auq2/WAqx2gNrL+q3RNjAzXpUfCXrzkA6d4Xa22yhRLy4AC50E+6UTPoscbo31nbOoq51gvkuXzJ6B2w==','333333333','Lord','2002-01-01','Darth','Vader','HOST'),('vli','1ARVn2Auq2/WAqx2gNrL+q3RNjAzXpUfCXrzkA6d4Xa22yhRLy4AC50E+6UTPoscbo31nbOoq51gvkuXzJ6B2w==','000000000','Student','2000-01-01','Vincent','Li','HOST'),('what','1ARVn2Auq2/WAqx2gNrL+q3RNjAzXpUfCXrzkA6d4Xa22yhRLy4AC50E+6UTPoscbo31nbOoq51gvkuXzJ6B2w==','888888888','Ex-President','2003-02-03','Donald','Trump','RENTER');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-08-08  7:20:16
