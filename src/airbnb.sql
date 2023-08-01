CREATE TABLE Listings (
    Listing_id int UNIQUE PRIMARY KEY,
    Listing_type varchar(10),
    Room_number int, 
    Is_active boolean,
    Price_per_day float
);

CREATE TABLE Amenities (
    Name varchar(100) NOT NULL UNIQUE PRIMARY KEY 
);

CREATE TABLE Users (
    Username varchar(100) UNIQUE PRIMARY KEY,
    Password varchar(100),
    SIN int UNIQUE,
    Occupation varchar(100),
    Date_of_birth DATE,
    First_name varchar(100),
    Last_name varchar(100),
    User_type varchar(10)
);

CREATE TABLE Locations (
    Longitude float,
    Latitude float, 
    Postal_code char(6),
    City varchar(100),
    Country varchar(100),
    Province varchar(100),
    PRIMARY KEY (Longitude, Latitude)
);

CREATE TABLE Payment_Info (
    Card_number int UNIQUE PRIMARY KEY,
    Security_Code int,
    Expiration_date DATE, 
    First_name varchar(100),
    Last_name varchar(100),
    Postal_code char(6)
);

CREATE TABLE Comments (
    Comment_id int UNIQUE PRIMARY KEY,
    Content varchar(225),
    Timestamp TIME 
);

CREATE TABLE Dates (
    Listing_id int UNIQUE,
    Start_date DATE,
    End_date DATE,
    PRIMARY KEY (Start_date, End_date)
);