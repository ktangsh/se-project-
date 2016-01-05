-- phpMyAdmin SQL Dump
-- version 4.0.10.10
-- http://www.phpmyadmin.net
--
-- Host: 127.9.210.2:3306
-- Generation Time: Nov 14, 2015 at 01:10 PM
-- Server version: 5.5.45
-- PHP Version: 5.3.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `smua`
--
CREATE DATABASE IF NOT EXISTS `smua` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `smua`;

-- --------------------------------------------------------

--
-- Table structure for table `App`
--

DROP TABLE IF EXISTS `App`;
CREATE TABLE IF NOT EXISTS `App` (
  `timestamp` datetime NOT NULL,
  `macAddress` varchar(255) NOT NULL DEFAULT '',
  `appID` int(11) DEFAULT NULL,
  PRIMARY KEY (`macAddress`,`timestamp`),
  KEY `FK_fr5ad11typ27co5ytf4akkpc4` (`appID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `AppLookup`
--

DROP TABLE IF EXISTS `AppLookup`;
CREATE TABLE IF NOT EXISTS `AppLookup` (
  `appID` int(11) NOT NULL,
  `appName` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`appID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Demographic`
--

DROP TABLE IF EXISTS `Demographic`;
CREATE TABLE IF NOT EXISTS `Demographic` (
  `macAddress` varchar(255) NOT NULL,
  `cca` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `gender` char(1) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`macAddress`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Location`
--

DROP TABLE IF EXISTS `Location`;
CREATE TABLE IF NOT EXISTS `Location` (
  `macAddress` varchar(255) NOT NULL,
  `timestamp` datetime NOT NULL,
  `locationID` int(11) DEFAULT NULL,
  PRIMARY KEY (`macAddress`,`timestamp`),
  KEY `FK_bq6keq5tu1jq8j5lm9ppdltyc` (`locationID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `LocationLookup`
--

DROP TABLE IF EXISTS `LocationLookup`;
CREATE TABLE IF NOT EXISTS `LocationLookup` (
  `locationID` int(11) NOT NULL,
  `semanticPlace` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`locationID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `App`
--
ALTER TABLE `App`
  ADD CONSTRAINT `FK_fr5ad11typ27co5ytf4akkpc4` FOREIGN KEY (`appID`) REFERENCES `AppLookup` (`appID`),
  ADD CONSTRAINT `FK_avgvc3k3hv0e1xwoqce3u8sbu` FOREIGN KEY (`macAddress`) REFERENCES `Demographic` (`macAddress`);

--
-- Constraints for table `Location`
--
ALTER TABLE `Location`
  ADD CONSTRAINT `FK_bq6keq5tu1jq8j5lm9ppdltyc` FOREIGN KEY (`locationID`) REFERENCES `LocationLookup` (`locationID`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
