establish_context
card_connect
select -AID A000000018434D
open_sc -security 0 -keyind 0 -keyver 0 -keyDerivation visa2 -key 47454d5850524553534f53414d504c45
get_status -element 40
card_disconnect
release_context