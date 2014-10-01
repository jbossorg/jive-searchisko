Jive Plugin: Searchisko integration
===================================

Jive plugin integrates [Searchisko](http://github.com/searchisko/searchisko).

Integration Features
--------------------

1. Notify Searchisko when user updates its profile


Configuration
-------------

In Jive system properties it's possible to configure this plugin:

### Common Configuration

1. `jbossorg.searchisko.url` - URL of searchisko e.g. https://dcp.jboss.org

2. `jbossorg.searchisko.auth.name` - username

3. `jbossorg.searchisko.auth.password` - password

### Profile Change Notification

1. `jbossorg.searchisko.profile.notify.enabled` - if `false` then notification is disabled. Default is `true`

2. `jbossorg.searchisko.profile.notify.interval` - interval how often (in seconds) plugin should notify Searchisko about updated profiles. All updated usernames are given in one request. Default is 10 seconds.

