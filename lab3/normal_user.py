import pika
import threading


class NormalUser:

    def __init__(self):
        self.info_connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
        # stworzenie kanalu do odbierania info od admina
        self.info_channel = self.info_connection.channel()
        # stworzenie exchange w tym celu
        self.info_channel.exchange_declare(exchange='info_exchange',
                                           exchange_type='fanout',
                                           auto_delete=True)
        # stworzenie i zbindowanie prywatnej kolejki
        result = self.info_channel.queue_declare('', auto_delete=True, exclusive=True)
        info_queue = result.method.queue
        self.info_channel.queue_bind(exchange='info_exchange',
                                     queue=info_queue,
                                     routing_key='')
        # subskrybcja na kolejce
        self.info_channel.basic_consume(queue=info_queue,
                                        on_message_callback=self.on_info)
        # nasluchiwanie na info od admina w osobnym watku
        info_thread = threading.Thread(target=self.info_channel.start_consuming)
        info_thread.start()

        self.log_connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
        # stworzenie kanalu do wysylania logow
        self.log_channel = self.log_connection.channel()
        # stworzenie log exhange
        self.log_channel.exchange_declare(exchange='log_exchange',
                                          exchange_type='fanout',
                                          auto_delete=True)

    def on_info(self, ch, method, props, body):
        print('INFO: ' + body.decode())
        ch.basic_ack(delivery_tag=method.delivery_tag)
