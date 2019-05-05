from __future__ import print_function

import random
import logging

import grpc

import exchange_pb2
import exchange_pb2_grpc


def subscribeToExchange(stub):
    cur_list = exchange_pb2.CurrenciesList
    stub.subscribeToExchange(cur_list)


# def guide_list_features(stub):
#     rectangle = route_guide_pb2.Rectangle(
#         lo=route_guide_pb2.Point(latitude=400000000, longitude=-750000000),
#         hi=route_guide_pb2.Point(latitude=420000000, longitude=-730000000))
#     print("Looking for features between 40, -75 and 42, -73")

#     features = stub.ListFeatures(rectangle)

#     for feature in features:
#         print("Feature called %s at %s" % (feature.name, feature.location))

def run():
    # NOTE(gRPC Python Team): .close() is possible on a channel and should be
    # used in circumstances in which the with statement does not fit the needs
    # of the code.
    with grpc.insecure_channel('localhost:50051') as channel:
        stub = exchange_pb2_grpc.ExchangeStub(channel)
        print("-------------- sub --------------")
        subscribeToExchange(stub)



if __name__ == '__main__':
    run()