import json
import os
import stat
import shutil
from pathlib import Path

output_folder_name = './output'
router_folder_name = output_folder_name + '/router'
terminal_folder_name = output_folder_name + '/terminal'
run_script_name = 'run.sh'
java_prefix = 'java -jar '
node_jar_name_with_dependencies = 'deusbase.node-1.0-jar-with-dependencies.jar'
terminal_jar_name_with_dependencies = 'deusbase.terminal-1.0-jar-with-dependencies.jar'
node_jar_name = 'deusbase.node-1.0-jar.jar'
terminal_jar_name = 'deusbase.terminal-1.0.jar'
node_target_folder = 'node/target' + "/" + node_jar_name_with_dependencies
terminal_target_folder = 'terminal/target' + "/" + terminal_jar_name_with_dependencies

def create_http_address(host_port):
    return 'http://' + host_port['host'] + ':' + host_port['port']

def create_run_script(executable_file_name, script_folder_path, program_args):
    script_path = script_folder_path + '/' + run_script_name
    java_command = java_prefix + executable_file_name
    for k, v in program_args.items():
        java_command += (' ' + k + '=' + v)


    with open(script_folder_path + '/' + run_script_name, 'w+') as text_file:
        text_file.write(java_command)

    st = os.stat(script_path)
    os.chmod(script_path, st.st_mode | stat.S_IEXEC)

if(Path(output_folder_name).exists()):
    shutil.rmtree(output_folder_name)

os.mkdir(output_folder_name)
os.mkdir(router_folder_name)
os.mkdir(terminal_folder_name)

shutil.copyfile(terminal_target_folder, terminal_folder_name + "/" + terminal_jar_name)
shutil.copyfile(node_target_folder, router_folder_name + "/" + node_jar_name)

with open('cluster_config.json') as json_file:
    data = json.load(json_file)

    router = data['router']

    create_run_script(node_jar_name, router_folder_name, { '-mode': 'router', '-host': router['host'], '-port': router['port'] })

    create_run_script(terminal_jar_name, terminal_folder_name, { '-url': create_http_address(router) })

    shard_list = data['shard_list']

    with open(router_folder_name + '/router_config.json', 'w') as outfile:
        json.dump(shard_list, outfile)

    for i in range(len(shard_list)):
        shard = shard_list[i]

        shard_name = 'shard_' + str(i)
        shard_folder_name = output_folder_name + '/' + shard_name
        os.mkdir(shard_folder_name)

        master_node_name = 'master'
        master_folder_name = shard_folder_name + '/' + master_node_name

        os.mkdir(master_folder_name)
        shutil.copyfile(node_target_folder, master_folder_name + "/" + node_jar_name)

        master = shard['master']
        create_run_script(node_jar_name, master_folder_name, { '-shard': shard_name, '-node': 'master', '-mode': 'master', '-host': master['host'], '-port': master['port'] })

        for j in range(len(shard['slaves'])):
            slave = shard['slaves'][j]
            slave_node_name = 'slave_' + str(j)
            slave_folder_name = shard_folder_name + '/' + slave_node_name

            os.mkdir(slave_folder_name)
            shutil.copyfile(node_target_folder, slave_folder_name + "/" + node_jar_name)

            create_run_script(node_jar_name, slave_folder_name, { '-shard': shard_name, '-node': slave_node_name, '-mode': 'slave', '-host': slave['host'], '-port': slave['port'], '-master_address': create_http_address(master) })

print('Cluster has been generated')
