## 4.4.0 update
+ /mc Discord slash command to run Minecraft commands, with autocompletion (`commands.slash_command`). The plain text commands prefix is deprecated
+ Account linking in public channels: add "account_linking" to a channel subscriptions, private messages can be disabled with `account.allow_private_messages`
+ Silent messages option per channel (`silent`), messages don't trigger notifications
+ Threads support in webhook mode
+ Improved variables: `${name|format}` formats dates, durations and numbers
+ Fix relay Discord messages with only attachments, and messages from other webhooks when `relay_bot_messages` is enabled
+ Show the reply prefix when replying to a webhook message (eg: a Minecraft player)
+ Mention Discord usernames with dots and channels with dashes from Minecraft
+ Allow entity selectors (@a, @p, ...) in Discord commands on 1.21.11+
+ Limit the failed link code attempts of a Discord user
+ Shorten Discord nicknames longer than 32 characters when renaming linked members
+ Report which privileged intent is missing (MESSAGE CONTENT or SERVER MEMBERS)
+ Report invalid allowed mentions instead of failing to start
+ Start the mod even if a channel or the members list cannot be fetched
+ Recreate the Mc2Discord webhook and send the message again when it was deleted
+ Run Discord messages and commands on the server thread
+ Log Discord request errors as a one line warning instead of a stacktrace
+ Do not update the status channels again when the mod restarts before the update period
+ Remove the update check
+ Fix death attacker variables reporting the victim instead of the killer
+ Fix death combat duration being in ticks instead of milliseconds
+ Fix `${attachments}` misspelled in the default chat format
+ Fix an invalid variable format breaking the whole message
+ Fix the say and me commands relayed twice on Fabric, and relayed for hidden players
+ Fix logs sent to Discord being lost when logged from several threads
+ Fix the default embed color of logs
+ Fix allowed commands matching on partial words (eg: "list" allowed "listall")
+ Fix commands failing when a link is not a valid url
+ Fix the account linked several times when several policies match
+ Fix player lists not reloaded when the command feedback is hidden
+ Fix reading the lang setting when it is not formatted as expected
+ Fix the token redaction of the config upload, which now uses HTTPS
+ Fix the Korean link code message
+ Fix connection reset errors
+ Keep handling Discord messages after an error
