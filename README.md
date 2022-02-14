# QuickShop-Suite
Power the QuickShop-Hikari with addons! This repo contains addons that from QuickShop-Reremake. And some dedicated addons for QuickShop-Hikari.

## List

Added a `/qs list` subcommand to allow player check the shops they're created at.

![qs list preview](https://user-images.githubusercontent.com/30802565/153894547-ff64045b-57e4-43f4-b321-0c427a59366f.png)

## Limited

Limit the amount that player buying/selling items to single shop.

### Command and Permission

* /qs limit set - Set the limit，Set -1 to remove limits and reset limit record.
* /qs limit unset - Remove limits and reset limit record.
* /qs limit period <hour/day/week/month/year> - Set limit record reset period. `Permission node: quickshop.limited`

![qs limited preview](https://user-images.githubusercontent.com/30802565/153894807-83ff6fc8-8e98-4ac4-9a89-f78136279711.png)

## ShopItemOnly

This plugin will block the player or hopper to insert the non-shop items into the shop container

![qs shopitemonly](https://user-images.githubusercontent.com/30802565/153895322-d879a1fa-2293-4c64-80ce-9c580bc7059e.png)
