/* Entities */
CREATE TABLE IF NOT EXISTS Listings (
    Listing_id char(36) UNIQUE PRIMARY KEY ,
    Listing_type varchar(10),
    Suite_number varchar(5),
    Max_guests int, 
    Is_active boolean,
    Time_listed Timestamp
);

CREATE TABLE IF NOT EXISTS Amenities (
    Name varchar(100),
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id char(36),
    CONSTRAINT PK_Amenities PRIMARY KEY (Name, Listing_id)
);

CREATE TABLE IF NOT EXISTS Users (
    Username varchar(100) UNIQUE PRIMARY KEY,
    Password varchar(100),
    SIN char(9) UNIQUE,
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
    Card_number char(16) UNIQUE PRIMARY KEY,
    Security_Code char(3),
    Expiration_date DATE, 
    First_name varchar(100),
    Last_name varchar(100),
    Postal_code char(6)
);

CREATE TABLE IF NOT EXISTS Availability (
    Start_date DATE,
    End_date DATE,
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id char(36),
    Price_per_day float,
    CONSTRAINT PK_Availability PRIMARY KEY (Start_date, Listing_id)
);

/* Relations */

CREATE TABLE IF NOT EXISTS Bookings (
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id char(36),
    FOREIGN KEY (Start_date) REFERENCES Availability(Start_date),
    Start_date DATE,
    FOREIGN KEY (Renter_id) REFERENCES Users(Username),
    Renter_id char(36),
    Total_price float,
    FOREIGN KEY (Card_number) REFERENCES Payment_info(Card_number),
    Card_number char(16),
    Cancelled_by char(36) DEFAULT "",
    CONSTRAINT PK_Bookings PRIMARY KEY (Listing_id, Start_date, Renter_id)
);

CREATE TABLE IF NOT EXISTS Host_of (
    FOREIGN KEY (Username) REFERENCES Users(Username),
    Username varchar(100),
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id char(36),
    CONSTRAINT PK_Host_of PRIMARY KEY (Username, Listing_id)
);

CREATE TABLE IF NOT EXISTS Rate_listing (
    FOREIGN KEY (Username) REFERENCES Users(Username),
    Username varchar(100),
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id char(36),
    Rating float,
    Timestamp Timestamp,
    CONSTRAINT PK_Rate_listing PRIMARY KEY (Username, Listing_id, Timestamp)
);

CREATE TABLE IF NOT EXISTS Rate_user (
    FOREIGN KEY (Reviewer) REFERENCES Users(Username),
    Reviewer varchar(100),
    FOREIGN KEY (Reviewee) REFERENCES Users(Username),
    Reviewee varchar(100),
    Rating float,
    Timestamp Timestamp,
    CONSTRAINT PK_Rate_User PRIMARY KEY (Reviewer, Reviewee, Timestamp)
);

CREATE TABLE IF NOT EXISTS Comment_listing (
    Comment_id char(36) UNIQUE,
    FOREIGN KEY (Username) REFERENCES Users(Username),
    Username varchar(100),
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id char(36),
    Text varchar(255),
    Timestamp Timestamp,
    CONSTRAINT PK_Comment_listing PRIMARY KEY (Comment_id)
);

CREATE TABLE IF NOT EXISTS Comment_user (
    Comment_id char(36) UNIQUE,
    FOREIGN KEY (Reviewer) REFERENCES Users(Username),
    Reviewer varchar(100),
    FOREIGN KEY (Reviewee) REFERENCES Users(Username),
    Reviewee varchar(100),
    Text varchar(255),
    Timestamp Timestamp,
    CONSTRAINT PK_Comment_user PRIMARY KEY (Comment_id)
);

CREATE TABLE IF NOT EXISTS Belongs_to (
    FOREIGN KEY (Listing_id) REFERENCES Listings(Listing_id),
    Listing_id char(36),
    Longitude float,
    Latitude float,
    CONSTRAINT FK_Belongs_to FOREIGN KEY (Longitude, Latitude) REFERENCES Locations(Longitude, Latitude),
    CONSTRAINT PK_Belongs_to PRIMARY KEY (Listing_id)
);

CREATE TABLE IF NOT EXISTS Paid_with (
    FOREIGN KEY (Username) REFERENCES Users(Username),
    Username varchar(100),
    FOREIGN KEY (Card_number) REFERENCES Payment_info(Card_number),
    Card_number char(16),
    CONSTRAINT PK_Paid_with PRIMARY KEY (Username, Card_number)
);