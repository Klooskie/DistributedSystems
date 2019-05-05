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
        
        currencies_list = request.currencies
        for currency in currencies_list:
            print(currency)
            currencyValue = exchange_pb2.CurrencyValue()
            currencyValue.currency = currency
            currencyValue.value = 2.50
            update = exchange_pb2.CurrencyUpdate(newCurrencyValue = currencyValue)
            yield update
                
        # left = min(request.lo.longitude, request.hi.longitude)
        # right = max(request.lo.longitude, request.hi.longitude)
        # top = max(request.lo.latitude, request.hi.latitude)
        # bottom = min(request.lo.latitude, request.hi.latitude)
        # for feature in self.db:
        #     if (feature.location.longitude >= left and
        #             feature.location.longitude <= right and
        #             feature.location.latitude >= bottom and
        #             feature.location.latitude <= top):
        #         yield feature

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    exchange_pb2_grpc.add_ExchangeServicer_to_server(
        ExchangeServicer(), server)
    server.add_insecure_port('[::]:50051')
    server.start()

    print("Exchange server started")
    
    try:
        while True:
            time.sleep(3)
    except KeyboardInterrupt:
        server.stop(0)


if __name__ == '__main__':
    serve()
