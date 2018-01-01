** Running **

./gradlew build && java -jar build/libs/ticketfaster.jar


** Usage **

A shell is launched to invoke the service methods of Ticket Faster. It operates similar to other shells so you can use arrow key up for example to recall the last command. Helpful for holding a lot of seats.


** Shell Commands **

seats - returns the number of available seats to hold

find [email] [seats] -  returns a seat hold for the number of seats requested. Also lists the seats held.

reserve [email] [seatHoldId] - attempts to reserve the given seat hold id if it hasn't expired and hasn't already been reserved.

held - lists all nonexpired seathold ids to reserve. Seathold ids already reserved will not be shown.


** Testing **

./gradlew test


** Assumptions **

-Just 1 JVM tracking this as the datasource.
-Thread safety added to implementation of seatholding because of conflict that can occur if 2 seats are held for the same person. Not added for reservations because there is no race condition (no contesting over same resource) to mixed outcome. A reservation can only happen per a held seat. There is no contesting over resources.
-Seats are found and retrieved in order of most desierable first. Seats are seen as most desirable in this order: how close the row is to the stage, if this is the seat in the middle of the row or closer to it than the other seats in that row.
-Seatholds are seen as most desirable if they can accomodate the entire party adjacently in a single row, searching for the most preferable seats first. If not, first come first serve of most preferred seats. If the preferred seats have any adjacent seats those are included so the seat hold could be a combination of multiple adjacent seats and non-adjacently reserved seats.
-Email is used/referenced somewhere else, but it is added to the SeatHold object for that caller. As there is no requirement for email lookup in the provided interface itself.


** Notes **

-Would be nice to add a transaction for data handling operations (eg: an unexpected exception could leave the data-structures out of sync, causing the application to not work properly or at all)
-Random test activity was done because there are so many combinations of seatholds and expirations.
-It would be better to score the seats ahead of time (eg: by distance to stage) than to iterate from the first row to the last (what if seats are diagonal to the stage?). And, it would alleviate the need for rows to be number index based (A map of scores -> seats is better abstraction in hindsight).


** Implementation Details **

-Seats are unheld after 2 minutes (configured with the Spring beans in Config.java)
-Checks to unhold seats occur every 1 second from a scheduled task in SeatHoldService.java.
-Seathold ids are 7 digits.
-Confirmation codes are a 5 in length and a mix of uppercase letters or digits.
