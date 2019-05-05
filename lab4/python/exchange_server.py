from concurrent import futures
import time
import math
import logging

import grpc
import sys

import exchange_pb2
import exchange_pb2_grpc

class ExchangeServicer(exchange_pb2_grpc.ExchangeServicer):

    def subscribeToExchange(self, request, context):
        print("ktos cos")
        currencies_list = request.currencies
        for currency in currencies_list:
            print(currency)
            currencyValue = exchange_pb2.CurrencyValue()
            currencyValue.currency = currency
            currencyValue.value = 2.50
            update = exchange_pb2.CurrencyUpdate(newCurrencyValue = currencyValue)
            yield update

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    exchange_pb2_grpc.add_ExchangeServicer_to_server(
        ExchangeServicer(), server)
    server.add_insecure_port('localhost:50051')
    server.start()

    print("Exchange server started")
    
    try:
        while True:
            time.sleep(3)
    except KeyboardInterrupt:
        server.stop(0)


if __name__ == '__main__':
    serve()
