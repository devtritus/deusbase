import json
import os
import shutil
from pathlib import Path

node_jar_name = "deusbase.node-1.0.jar"
terminal_jar_name = "deusbase.terminal-1.0.jar"

output_folder_name = "./output"
cluster_folder_name = output_folder_name + "/cluster"
router_folder_name = output_folder_name + "/router"
terminal_folder_name = output_folder_name + "/terminal"
run_script_name = "run.sh"
java_prefix = "java -jar "

def create_run_script(executable_file_name, script_folder_path, program_args):
    java_command = java_prefix + executable_file_name
    for k, v in program_args.items():
        java_command += (" " + k + "=" + v)

    with open(script_folder_path + "/" + run_script_name, "w+") as text_file:
        text_file.write(java_command)

if(Path(output_folder_name).exists()):
    shutil.rmtree(output_folder_name)

os.mkdir(output_folder_name)
os.mkdir(cluster_folder_name)
os.mkdir(router_folder_name)
os.mkdir(terminal_folder_name)

create_run_script(node_jar_name, router_folder_name, { "-mode": "router" })

with open('cluster_config.json') as json_file:
    data = json.load(json_file)

    create_run_script(terminal_jar_name, terminal_folder_name, { "-url": data['router'] })

    shard_list = data['shard_list']

    with open(router_folder_name + '/router_config.json', 'w') as outfile:
        json.dump(shard_list, outfile)

    for i in range(len(shard_list)):
        shard = shard_list[i]

        shard_folder_name = cluster_folder_name + "/shard_" + str(i)
        os.mkdir(shard_folder_name)

        master_folder = shard_folder_name + "/master"
        os.mkdir(master_folder)
        create_run_script(node_jar_name, master_folder, { "-mode": "master", "-port": "3334", "-scheme": "master" })
        print('Master: ' + shard['master'])

        for j in range(len(shard['slaves'])):
            slave_folder = shard_folder_name + "/slave_" + str(j)
            os.mkdir(slave_folder)
            create_run_script(node_jar_name, slave_folder, { "-mode": "slave", "-port": "3334", "-scheme": "slave" })
            print('Slave: ' + shard['slaves'][j])
