package io.dwak.reddit.bot.model.slack

data class WebHookPayload(val text : String,
                          val attachments : List<WebHookPayloadAttachment>)

data class WebHookPayloadAttachment(val text : String,
                                    val fallback : String,
                                    val callback_id : String,
                                    val attachment_type : String? = "default",
                                    val actions : List<WebHookPayloadAction>)

data class WebHookPayloadAction(val name : String,
                                val text : String,
                                val type : String = "button",
                                val value : String)
