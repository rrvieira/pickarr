# pickarr

Pickar is a service (distributed in a docker image) that keeps an eye on trending movies/tvshows on [IMDb](https://www.imdb.com/) and recommend the ones that might be of interest, according to some user defined criteria. The recommendations are sent via [Telegram](https://telegram.org/) messages. For each movie/tvshow recommendation it includes some related data:
- Title and year of release;
- Studio/network
- Synopsis
- IMDb rating and number of votes
- Popularity ranking
- Genres

and provides the following actions:
- Open IMDb movie/tvshow details page
- Add to [Radarr](https://github.com/Radarr/Radarr)/[Sonarr](https://github.com/Sonarr/Sonarr)

### Recommendation Messages - Demo

<p float="left">
  <img width=310 src="https://user-images.githubusercontent.com/3785821/138491362-41981e8a-c129-4eab-af63-d2e18b15fd76.png">
  <img width=310 src="https://user-images.githubusercontent.com/3785821/138491380-70201e81-7015-4b87-8df3-d4dd6265b54c.png">
</p>


# Usage

Here are some example snippets to show how one can get started creating a container.

## Starting the container

### docker-compose

`docker-compose.yml`
```yaml
version: "2.4"

pickarr:
  container_name: pickarr
  image: rrvieir4/pickarr:latest
  restart: always
  ports:
    - "7877:7877"
  environment:
    - PUID=1000
    - PGID=1000
    - TZ=Europe/Lisbon
    - RADARR_URL=http://127.0.0.1:7878
    - RADARR_API_KEY=<your radarr api key>
    - RADARR_QUALITY_PROFILE_NAME=Ultra-HD
    - SONARR_URL=http://127.0.0.1:8989
    - SONARR_API_KEY=<your sonarr api key>
    - SONARR_QUALITY_PROFILE_NAME=Ultra-HD
    - MOVIE_MIN_YEAR=2021
    - MOVIE_MIN_VOTES=3000
    - MOVIE_MIN_RATING=5.7
    - TV_SHOW_MIN_YEAR=2021
    - TV_SHOW_MIN_VOTES=3000
    - TV_SHOW_MIN_RATING=7.5
    - TAG_NAME=pickarr
    - REFRESH_INTERVAL=86400
    - ACTION_ADDRESS=http://127.0.0.1:7877
    - TELEGRAM_USER_TOKEN=<your telegram user token>
    - TELEGRAM_CHAT_ID=<your telegram chat id>
  volumes:
    - /your/path/to/pickarr/data:/data
```

Launch:
```console
foo@bar:~$ docker-compose --file docker-compose.yml up -d --remove-orphans
```

### docker cli

```console
docker run -d \
  --name=pickarr \
  -e PUID=1000 \
  -e PGID=1000 \
  -e TZ=Europe/Lisbon \
  -e RADARR_URL='http://127.0.0.1:7878' \
  -e RADARR_API_KEY=<your radarr api key> \
  -e RADARR_QUALITY_PROFILE_NAME=Ultra-HD \
  -e SONARR_URL='http://127.0.0.1:8989' \
  -e SONARR_API_KEY=<your sonarr api key> \
  -e SONARR_QUALITY_PROFILE_NAME=Ultra-HD \
  -e MOVIE_MIN_YEAR=2021 \
  -e MOVIE_MIN_VOTES=3000 \
  -e MOVIE_MIN_RATING=5.7 \
  -e TV_SHOW_MIN_YEAR=2021 \
  -e TV_SHOW_MIN_VOTES=3000 \
  -e TV_SHOW_MIN_RATING=7.5 \
  -e TAG_NAME=pickarr \
  -e REFRESH_INTERVAL=86400 \
  -e ACTION_ADDRESS='http://127.0.0.1:7877' \
  -e TELEGRAM_USER_TOKEN=<your telegram user token> \
  -e TELEGRAM_CHAT_ID=<your telegram chat id> \
  -p 7877:7877 \
  -v /your/path/to/pickarr/data:/data \
  --restart unless-stopped \
  rrvieir4/pickarr:latest
```

### Parameters

Name | Optional? | Default | Description
------------ | ------------- | ------------- | -------------
RADARR_URL | No | N/A | Radarr address
RADARR_API_KEY | No | N/A | Radarr API Key
RADARR_QUALITY_PROFILE_NAME | No | N/A | Radarr quality profile name (case-sensitive) for the movies added by pickarr
SONARR_URL | No | N/A | Sonarr address
SONARR_API_KEY | No | N/A | Sonarr API Key
SONARR_QUALITY_PROFILE_NAME | No | N/A | Sonarr quality profile name (case-sensitive) for the tvshows added by pickarr
DEFAULT_TAG_NAME | Yes | pickarr | Tag name for the movies/tvshows added by pickarr
REFRESH_INTERVAL | Yes | 86400 | Popular list refresh interval (in seconds)
ACTION_ADDRESS | No | N/A | pickarr address
TELEGRAM_USER_TOKEN | No | N/A | Your telegram user token
TELEGRAM_CHAT_ID | No | N/A | Your telegram chat id
MOVIE_MIN_YEAR | Yes | 2021 | Movie's minimum release year to be eligible for recommendation
MOVIE_MIN_VOTES | Yes | 3000 | Movie's minimum number of votes to be eligible for recommendation
MOVIE_MIN_RATING | Yes | 5.7 | Movie's minimum rating to be eligible for recommendation
TV_SHOW_MIN_YEAR | Yes | 2021 | Tvshow's minimum release year to be eligible for recommendation
TV_SHOW_MIN_VOTES | Yes | 3000 | Tvshow's minimum number of votes to be eligible for recommendation
TV_SHOW_MIN_RATING | Yes | 7.5 | Tvshow's minimum release year to be eligible for recommendation

