# LOA-EUW-Community-Discord-Bot

This bot checks the status page of LostARK (EU) at predefined intervals and displays any changes in a Discord channel
In the newer versions the Bot also check the website LostMerchants.com for rare Items on both Servers (Nia, Ealyn) and prints
them to a specific channel and deletes them again if they expire

Required default Textchannels (Can be changed via /config):

- "loa-euw-status"
- "loa-euw-notify"
- "loa-euw-merchants"
Commands:

Can be executed by any Users:
- /ping - Test the Bot response
- /about - Get Information about the Bot

Can be executed by Users with Permissions or Server-owners:
- /config - Update the Channel Names (Push, Status, Merchants) or deactivate Push Notifications ("loabot.config") (Server-owner has it regardless)
- /permissions - Change permissions from the users on your Discord regard the Bot ("loabot.permissions") (Server-owner has it regardless) (Others cannot change owner)

Can be only executed by the hard-coded Dev:
- /update - You need to have a Script for this (Updates from Repo) (Only Dev has Permission for this)
- /restart - You need to have a Script for this (Restarts the Bot) (Only Dev has Permission for this)
- /stop - Stop the Bot (Only Dev has Permission for this)
- /reload - Reload configs (for all Servers) (Only Dev has Permission for this)

