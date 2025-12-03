# KillrVideo (v2) Data API Loader

A console-based Java app which loads Patrick's video.csv file(s) into a collection-based KillrVideo database.

## Prerequisites
1. Java 21
2. A local `data/` directory containing files from https://github.com/KillrVideo/killrvideo-data/tree/master/data/csv :
 - videos.csv
 - video_ratings.csv
 - video_playback_stats.csv

3. Environment variables:
 - `ASTRA_DB_API_ENDPOINT`
 - `ASTRA_DB_APPLICATION_TOKEN`
 - `ASTRA_DB_NAMESPACE`
 - `DATA_DIR`

## Buidling
1. Clone this repository.
2. run:
```shell
mvn clean install
```

## Running
 - Download `video_*.csv` files (mentioned above).
 - Create `data/` dir and copy `video_*.csv` files into it:
```shell
mkdir data
cp ~/Downloads/video_*.csv data/
```
 - run:
```shell
java -jar target/killrvideo-dataapi-loader-0.0.1-SNAPSHOT.jar
```
