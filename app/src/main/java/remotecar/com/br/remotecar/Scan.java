package remotecar.com.br.remotecar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class Scan extends Activity {
	ListView lista;
	List<Device> devices;
	BluetoothAdapter bluetoothAdapter;
	BluetoothDevice bluetoothDevice;
	DevicesArrayAdapter adapter;
	Main main;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_activity);
		lista = (ListView) findViewById(R.id.listView1);
		
		main = new Main();
		
		findDevices();

		lista.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Device d = devices.get(position);

				//Retorna os dispositivos pareados e seleciona o que foi clicado na ListView
				Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
				if (pairedDevices.size() > 0) {
					for (BluetoothDevice device : pairedDevices) {
						if (device.getAddress().equals(d.getMacAddress())){
//							d.setDevice(device);
							d.setMacAddress(device.getAddress());
							d.setName(device.getName());
							bluetoothDevice = device;
							break;
						}
					}
				}
//				String descricao = "Selecionado: " + d.getName() + "(" + d.getMacAddress() + ")";
//				Toast.makeText(getApplicationContext(), descricao, Toast.LENGTH_SHORT).show();
				Intent i = new Intent(Scan.this, Main.class);
				Bundle params = new Bundle();
				params.putBoolean("status", true);
				i.putExtra("device", d);
				i.putExtras(params);
				Scan.this.startActivity(i);
				Scan.this.finish();
			}

		});
	}

	//Busca dispositivos j� pareados
	void findDevices() {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

		if (pairedDevices.size() > 0) {

			devices = new ArrayList<Device>();

			for (BluetoothDevice device : pairedDevices) {

				devices.add(new Device(device.getName(), device.getAddress()));
			}

			// Add o nome e endereco no ArrayAdapter para ser visualizado na ListView
			adapter = new DevicesArrayAdapter(this, devices);
			lista.setAdapter(adapter);

		}
	}
	

}
