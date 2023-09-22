## Postman tests sequence

```mermaid
flowchart TD
    1[First user Admin] --> 2[2nd user Merchant]
    2 --> 3[3rd user Merchant]
    3 --> 4[Unlock 2nd user]
    4 --> 5[Unlock 3rd user]
    5 --> 6[3rd user access is Support] --> 7[2nd user makes transaction]
    7 --> 8[3rd user get's users list] --> 9[3rd user adds stolen cards and sus. IPs]
    9 --> 10[3rd user gets list of stolen cards/IPs] --> 11[2nd user makes transaction]
    11 --> 12[3rd user deletes stolen cards/IPs] --> 13[2nd user makes more transactions]
    13 --> 14[3rd user gets the list of transactions]
%%    new operations goes here
    14 --> 
```