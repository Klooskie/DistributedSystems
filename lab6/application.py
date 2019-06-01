from kazoo.client import KazooClient, KazooState
from kazoo.recipe.watchers import DataWatch, ChildrenWatch
import logging
import argparse
import subprocess    

# Run outer application using subprocess library
def run_app():
    global application_to_run
    global application_process
    application_process = subprocess.Popen(application_to_run)
    print("Started application \"" + str(application_to_run) + "\"")

# Kill outer application using subprocess library
def kill_app():
    global application_to_run
    global application_process
    application_process.terminate()
    print("Closed application \"" + str(application_to_run) + "\"")

# Listens for connection state changes
def state_listener(state):
    if state == KazooState.LOST:
        print('Disconnected from servers')
    elif state == KazooState.SUSPENDED:
        print('Connection to servers was lost, trying to reconnect')
    elif state == KazooState.CONNECTED:
        print('Connected to servers properly')

# Watches changes of /z node children
def watch_children(children):
    if len(children) == 0:
        print("Number of children of the /z node is now", len(children))
    else:
        print("Number of children of the /z node is now", len(children), children)

# Watches changes of /z node itself
def watch_node(data, stat):
    global z_already_exists

    if not stat and z_already_exists: 
        # when /z ig getting closed - kill the app
        print("The /z node has been closed")
        kill_app()
        z_already_exists = False
    
    elif stat and not z_already_exists:
        # when /z node exists and we did not know that before - run the app and start watching z's children
        print("The /z node exists")
        run_app()
        zk.ChildrenWatch("/z", watch_children)
        z_already_exists = True

# Visualizes tree of nodes being /z node's children
def visualize_z_tree(depth=0, node="/z"):
    if zk.exists(node):
        if depth > 0:
            print((depth - 1) * 2 * ' ' + u'\u2ba1', node.split('/')[-1])
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

    # start kazoo cliend, then add state listener and /z node watcher 
    zk = KazooClient(hosts=hosts)
    zk.start()
    zk.add_listener(state_listener)
    zk.DataWatch("/z", watch_node)

    # handle user commands
    while True:
        command = input('Type the command ( tree | quit )\n')
        if command == 'tree':
            visualize_z_tree()
        elif command == 'quit':
            if z_already_exists:
                kill_app()
            break
        else:
            print('Incorrect command')

    zk.stop()
    
