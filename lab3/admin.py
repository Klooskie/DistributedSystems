import pika
import threading
import uuid


class Admin:

    def __init__(self):
        self.sending_connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
        self.listening_connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))

        # stworzenie kanalow do wysylania i nasluchiwania
        self.sending_channel = self.sending_connection.channel()
        self.listening_channel = self.listening_connection.channel()

        # stworzenie exchange do wysylania info
        self.sending_channel.exchange_declare(exchange='admin_exchange',
                                              exchange_type='fanout',
                                              auto_delete=True)

        # stworzenie exchange do odbierania logow
        self.sending_channel.exchange_declare(exchange='log_exchange',
                                              exchange_type='fanout',
                                              auto_delete=True)

        # stworzenie i zbindowanie prywatnej kolejki do odbierania logow
        result = self.listening_channel.queue_declare('', auto_delete=True, exclusive=True)
        listening_queue = result.method.queue
        self.listening_channel.queue_bind(exchange='log_exchange',
                                          queue=listening_queue,
                                          routing_key='')

        # subskrybcja na kolejce do odbierania logow
        self.listening_channel.basic_consume(queue=listening_queue,
                                             on_message_callback=self.handle_log_message)

        # rozpoczecie nasluchiwania na kolejce logow w nowym watku
        listening_thread = threading.Thread(target=self.listening_channel.start_consuming)
        listening_thread.start()

    def broadcast_info(self, content):
        message = content
        self.sending_channel.basic_publish(exchange='admin_exchange',
                                           routing_key='',
                                           body=message)

    def handle_log_message(self, ch, method, props, body):
        print('Log: ' + body.decode())
        ch.basic_ack(delivery_tag=method.delivery_tag)


if __name__ == "__main__":

    admin = Admin()
    print("Wpisz wiadomosc do rozeslania do wszystkich")
    while True:
        input_line = input()
        admin.broadcast_info(input_line)
