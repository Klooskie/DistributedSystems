from kazoo.client import KazooClient, KazooState
from kazoo.recipe.watchers import DataWatch, ChildrenWatch
import logging
import argparse
import subprocess    

def run_app():
    global application_to_run
    global application_process
    application_process = subprocess.Popen(application_to_run)
    print("Started application \"" + str(application_to_run) + "\"")

def kill_app():
    global application_to_run
    global application_process
    application_process.terminate()
    print("Closed application \"" + str(application_to_run) + "\"")

def my_listener(state):
    if state == KazooState.LOST:
        print('Disconnected')
        # Register somewhere that the session was lost
    elif state == KazooState.SUSPENDED:
        print('Connection to server was lost, trying to reconnect')
        # Handle being disconnected from Zookeeper
    elif state == KazooState.CONNECTED:
        print('Connected')
        # Handle being connected/reconnected to Zookeeper

def watch_children(children):
    if len(children) == 0:
        print("Number of children of the /z node is now", len(children))
    else:
        print("Number of children of the /z node is now", len(children), children)

def watch_node(data, stat):
    global z_already_exists
    if not stat and z_already_exists: 
        print("The /z node has been closed")
        kill_app()
        z_already_exists = False
    elif stat and not z_already_exists:
        print("The /z node exists")
        run_app()
        zk.ChildrenWatch("/z", watch_children)
        z_already_exists = True

def visualize_z_tree(depth=0, node="/z"):
    if zk.exists(node):
        if depth > 0:
            print((depth - 1) * ' ' + u'\u21b3' + node.split('/')[-1])
        else:
            print(node.split('/')[-1])

        for child in zk.get_children(node):
            visualize_z_tree(depth=(depth + 1), node=(node + "/" + child))
    else:
        print("The node", node, "does not exist")
    
if __name__ == "__main__":
    # logging setup reccomended when using kazoo
    logging.basicConfig()

    # argument parser setup
    parser = argparse.ArgumentParser(description='Script description TODO')
    parser.add_argument('application', metavar='"app"', help='application to run after creating znode /z')
    application_to_run = parser.parse_args().application
    
    print("Aplication to run when /z node exists is \"" + application_to_run + "\"")

    z_already_exists = False
    application_process = None

    servers_addresses = ['127.0.0.1:2181', '127.0.0.1:2182', '127.0.0.1:2183']
    hosts = ','.join(servers_addresses)

    zk = KazooClient(hosts=hosts)
    zk.start()

    zk.add_listener(my_listener)
    zk.DataWatch("/z", watch_node)

    while True:
        command = input('Type the command ( tree | quit )\n')

        if command == 'tree':
            visualize_z_tree()
        elif command == 'quit':
            kill_app()
            break
        else:
            print('Incorrect command')

    zk.stop()
    
