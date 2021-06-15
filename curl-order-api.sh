curl --location --request POST 'http://localhost:8080/order' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "test",
    "cardType": "MASTERCARD",
    "discount": 10,
    "price": 100000
}'
