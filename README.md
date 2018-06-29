# Photo Paper is dead

On June 15th 2018 500px [shut off their API](https://support.500px.com/hc/en-us/articles/360002435653-API-). This app no longer functions without access to the 500px API.

## Photo Paper for Android

### Why

There weren't any free/open source 500px wallpapers so I decided to make one.

### How

Photo Paper isn't a wallpaper per se, but an app that periodically updates your wallpaper.
It doesn't run in the background except when changing the wallpaper, which only takes a few seconds.
This mean one less service running and eating resources.

### API Key

When running the app you need to set a consumer key and consumer secret in the `gradle.properties`
file by adding the following lines:

```ini
systemProp.consumer_key="KEY"
systemProp.consumer_secret="SECRET"
```

The consumer key and consumer secret are required to access the 500px API.
API keys can generated with an account from [500px's developer page](http://developers.500px.com/).

### License

Photo Paper is open source and available under the MIT license, see the [LICENSE](LICENSE) file for more info.

Photo Paper uses the [500px API](http://developers.500px.com/) to access photos and operates under [500px's](http://500px.com/)
[terms of use](https://github.com/500px/api-documentation/blob/master/basics/terms_of_use.md),
but is not endorsed or certified by 500px. Photos and images from 500px are owned by 500px
members and not by me or 500px. All rights not expressly granted to you are reserved by 500px and/or its members.
