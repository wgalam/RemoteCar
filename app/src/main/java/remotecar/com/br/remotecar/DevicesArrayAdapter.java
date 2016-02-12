package remotecar.com.br.remotecar;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class DevicesArrayAdapter extends ArrayAdapter<Device>{
	
	Context context;
	List<Device> devices;

	public DevicesArrayAdapter(Context context, List<Device> dev) {
		super(context, 0);
		this.context = context;
		this.devices = dev;
	}
	
	@Override
	public int getCount() {
		return devices.size();
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {

		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.device_item, null);
		}

		TextView txtName = (TextView) view.findViewById(R.id.txtName);
		TextView txtMacAddress = (TextView) view.findViewById(R.id.txtMacAddress);

		Device device = devices.get(position);

		txtName.setText(device.getName());
		txtMacAddress.setText(device.getMacAddress());

		return view;
	}
	
	@Override
	public Device getItem(int position) {
		return devices.get(position);
	}

}
