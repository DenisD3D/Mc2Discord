## Mc2Discord 3.2.6
### Added
 + 1.19 Support
 + Korean (ko_kr) translation thanks to PixVoxel
 + Link for streaming presence type (must be YouTube or Twitch)
 + Unlink players from Discord when they leave the discord server
 + Added warning in the 1.12.2 config file for features disabled in that version
 + Add link add|remove|list|reload commands to manage linked players
 + Replace mention by @display_name

### Fixed
 + 1.12.2 command permission issue
 + Fix account linking not working for new players
 + Split webhook message into multiple messages if > 6000 characters to prevent errors
 + Fix custom emoji replacement to :emoji: form