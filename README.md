Reddit Mod SlackBot
===

This bot should help moderate your subreddit via slack

make sure you create the following files

`bot/src/main/resources/reddit-config.json`

```json
{
  "subreddit" : "<subreddit>",
  "reddit_username" : "<bot_username>",
  "reddit_pwd" : "<bot_password>",
  "reddit_client_id" : "<reddit_client_id>",
  "reddit_client_secret" : "<reddit_client_secret>"
}
```

`bot/src/main/resources/slack-config.json`

```json
{
  "slack_key" : "<slack_key>",
  "slack_token" : "<slack_token>",
  "slack_verification_token": "<slack_verification_token>"
}
```

or set the previous properties as system environment variables
