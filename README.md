# JAVA (back-end) Developer Recruitment Assessment

**CMC Developer - Tuan Ngo**

## Table of contents
* [General info](#general-info)
* [Technologies](#technologies)
* [Setup](#setup)
* [Requirements](#requirements)
* [URL](#url)
* [Notes](#notes)

## General info
Web Service to query coin data from Coin-Gecko

## Technologies
* Java 1.11
* Spring Boot 2.7.0
* PostgreSQL 42.3.0 

## Setup
**Prerequisites:**

You need to update the database config to point to your desired database first.

**Run:**

Preferably import the project into your favorite IDE and run the project.


## URL

``````
curl --location --request GET 'localhost:8080/api/v1/coins/get_coins' \
--header 'Content-Type: application/json' \
--data-raw '{
    "currency": "usd",
    "per_page": 10,
    "page": 1
}'
``````
- There are validations for the request body:
  - currency must be existing in the supported currency list
  - page starts from 0
  - per_page falls between 1 - 250


## Requirements
- Create API [get_coins] to query data from coingecko server and return response with predefined structure
- This API must support in case of theapi.coingecko.com is unreachable (network error, site is down, etc.)

## Notes
- Two Gecko APIs were provided to get coin data list by currency and get coin detailed data. This should be sufficient for me to develop a web service acting as a proxy server to retrieve and reformulate the data.
- However, as one of the requirement is to make sure the API can run when CoinGecko server is down, I develop addition functionalities to query and cache coin data. This is mainly to overcome the situation that CoinGecko limits the API call number at 50 queries/minute. If we call getCoins API to get a list of more than 50 items, it probably will not be able to fetch all the expected data.
  1. When the server first starts, call into CoinGecko to get the list of all available currencies (61), and coins (aprox. 13k, at the moment of my development)
  2. A TaskScheduler is created to run in the background to crawl the listing data of all coins (lacking description and tradeUrl) per currency. 
     1. The CoinGecke API used for this crawling activity is the same getList API provided, as it can query 250 coin data per call 
     2. In order to support this feature, I prioritize a few currencies (can be configured in the application.properties) 
     3. The priority of a currency will be changed if the getCoins API is called with a different currency
     4. Understanding that many coins are unpopular (price = 0, market cap range > 2000, supposedly), but there may be a need to query them so I let the crawl do it anyway. It will take ~ 2 mins to query all the coins data per currency and about 2 hours to query all the data. This time can be reduced if we neglect coins after 2000th range
     5. An additional field of lastUpdatedTime is created for coin entity in the DB. Using this field, I will skip crawling the data for coin that are recently updated. The customizable period setting is 12 minutes (0.2 hour)
  3. GetCoins API will also save coin data into database, fetching the description and tradeUrl that are only available in the CoinGecko's getCoinDetail API

