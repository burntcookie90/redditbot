Reddit Mod SlackBot
===

This bot should help moderate your subreddit via slack

make sure you create the following file `bot/src/main/resources/config.json`

```json
{
  "subreddit" : "<subreddit>",
  "reddit_username" : "<bot_username>",
  "reddit_pwd" : "<bot_password>",
  "reddit_client_id" : "<reddit_client_id>",
  "reddit_client_secret" : "<reddit_client_secret>",
  "slack_key" : "<slack_key>",
  "slack_token" : "<slack_token>",
  "channel_id" : "<slack_channel_id>"
}
```

or set the previous properties as system environment variables