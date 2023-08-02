-- DROP TABLE IF EXISTS Has_amenity;
-- DROP TABLE IF EXISTS Stayed_at;
-- DROP TABLE IF EXISTS Host_of;
-- DROP TABLE IF EXISTS Rate_listing;
-- DROP TABLE IF EXISTS Rate_user;
-- DROP TABLE IF EXISTS Comment_listing;
-- DROP TABLE IF EXISTS Comment_user;
-- DROP TABLE IF EXISTS Avaliable_on;
-- DROP TABLE IF EXISTS Belongs_to;
-- DROP TABLE IF EXISTS Paid_with;
-- DROP TABLE IF EXISTS Listings;
-- DROP TABLE IF EXISTS Amenities;
-- DROP TABLE IF EXISTS Users;
-- DROP TABLE IF EXISTS Locations;
-- DROP TABLE IF EXISTS Payment_info;
-- DROP TABLE IF EXISTS Comments;
-- DROP TABLE IF EXISTS Dates;



/* Entities */
CREATE TABLE IF NOT EXISTS Listings (
    Listing_id int UNIQUE PRIMARY KEY,
    Listing_type varchar(10),
    Suite_number int, 
    Is_active boolean,
    Price_per_day float
);

CREATE TABLE IF NOT EXISTS Amenities (
    Name varchar(100) NOT NULL UNIQUE PRIMARY KEY 
);

CREATE TABLE IF NOT EXISTS Users (
    Username varchar(100) UNIQUE PRIMARY KEY,
    Password varchar(100),
    SIN int UNIQUE,
    Occupation varchar(100),
    Date_of_birth DATE,
    First_name varchar(100),
    Last_name varchar(100),
    User_type varchar(10)
);

CREATE TABLE IF NOT EXISTS Locations (
    Longitude float,
    Latitude float, 
    Postal_code char(6),
    City varchar(100),
    Country varchar(100),
    Province varchar(100),
    CONSTRAINT PK_Locations PRIMARY KEY (Longitude, Latitude)
);

CREATE TABLE IF NOT EXISTS Payment_info (
    Card_number int(16) UNIQUE PRIMARY KEY,
    Security_Code int,
    Expiration_date DATE, 
    First_name varchar(100),
    Last_name varchar(100),
    Postal_code char(6)
);

CREATE TABLE IF NOT EXISTS Comments (
    Comment_id int UNIQUE PRIMARY KEY,
    Content varchar(255),
    Timestamp TIME 
);

CREATE TABLE IF NOT EXISTS Dates (
    Listing_id int UNIQUE,
    Start_date DATE,
    End_date DATE,
    PRIMARY KEY (Start_date, End_date)
);


/* Relations */

CREATE TABLE IF NOT EXISTS Has_amenity (
    FOREIGN KEY (Name) REFERENCES Amenities(Name),
    Name varchar(100),
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id int
);

CREATE TABLE IF NOT EXISTS Stayed_at (
    FOREIGN KEY (Username) REFERENCES Users(Username),
    Username varchar(100),
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id int,
    Total_price float,
    Card_number int
);

CREATE TABLE IF NOT EXISTS Host_of (
    FOREIGN KEY (Username) REFERENCES Users(Username),
    Username varchar(100),
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id int
);

CREATE TABLE IF NOT EXISTS Rate_listing (
    FOREIGN KEY (Username) REFERENCES Users(Username),
    Username varchar(100),
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id int,
    Rating float
);

CREATE TABLE IF NOT EXISTS Rate_user (
    FOREIGN KEY (Host) REFERENCES Users(Username),
    Host varchar(100),
    FOREIGN KEY (Renter) REFERENCES Users(Username),
    Renter varchar(100)
);

CREATE TABLE IF NOT EXISTS Comment_listing (
    FOREIGN KEY (Comment_id) REFERENCES Comments(Comment_id),
    Comment_id int,
    FOREIGN KEY (Username) REFERENCES Users(Username),
    Username varchar(100),
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id int
);

CREATE TABLE IF NOT EXISTS Comment_user (
    FOREIGN KEY (Comment_id) REFERENCES Comments(Comment_id),
    Comment_id int,
    FOREIGN KEY (Host) REFERENCES Users(Username),
    Host varchar(100),
    FOREIGN KEY (Renter) REFERENCES Users(Username),
    Renter varchar(100)
);

CREATE TABLE IF NOT EXISTS Avaliable_on (
    FOREIGN KEY (Start_Date) REFERENCES Dates(Start_date),
    Start_date DATE, 
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id int
);

CREATE TABLE IF NOT EXISTS Belongs_to (
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id int,
    Longitude float,
    Latitude float,
    CONSTRAINT FK_Belongs_to FOREIGN KEY (Longitude, Latitude) REFERENCES Locations(Longitude, Latitude)
);

CREATE TABLE IF NOT EXISTS Paid_with (
    FOREIGN KEY (Host) REFERENCES Users(Username),
    Host varchar(100),
    FOREIGN KEY (Card_number) REFERENCES Payment_info(Card_number),
    Card_number int(16)
);