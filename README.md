# 500px Wallpaper for Android

## Why

There weren't any free/open source 500px wallpapers so I decided to make one.

## How

500px Wallpaper isn't a wallpaper per se, but an app that periodicly updates your wallpaper.
It doesn't run in the background except when changing the wallpaper, which only takes a few seconds.
This mean one less service running and eating resources.

## API Key

When running the app you need to set a consumer key in the `gradle.properties` file by adding the line `systemProp.consumer_key=KEY` to access the 500px API. API keys can generated with an account from [500px's developer page](http://developers.500px.com/).

## License

500px-wallpaper-android is open source and available under the MIT license, see the [LICENSE](LICENSE) file for more info.
500px-wallpaper-android uses the [500px API](http://developers.500px.com/) to access photos and operates under [500px's](http://500px.com/)
[terms of use](https://github.com/500px/api-documentation/blob/master/basics/terms_of_use.md). Photos and images from 500px are owned by 500px
members and not by me or 500px. All rights not expressly granted to you are reserved by 500px and/or its members.
