package remotecar.com.br.remotecar;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Projeto Controle de Arduino via Bluetooth
 * @version 1.0
 */
public class Main extends Activity {
	static List<Integer> recList = new ArrayList<Integer>();
	static boolean recording = false;
	static boolean play = false;
	boolean playing = true;
	TextView status;
	TextView position;
	BluetoothAdapter mBluetoothAdapter;
	BluetoothSocket mmSocket;
	BluetoothDevice mmDevice;
	boolean checkStatus = false;
	OutputStream mmOutputStream;
	InputStream mmInputStream;
	Thread workerThread;
	byte[] readBuffer;
	int readBufferPosition;
	volatile boolean stopWorker;
	private SensorManager mSensorManager;
	private float mSensorX;
	private float mSensorY;
	private float mSensorZ;
	private SimulationView simulation;
	public boolean live;
	public boolean send;
	public boolean acelerate;
	public boolean reverse;
	public int sendRateSleepTime;
	protected PowerManager.WakeLock mWakeLock;
	int anglebound;
	static boolean byEditor = false;

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event)
//	{
//		//replaces the default 'Back' button action
//		if(keyCode==KeyEvent.KEYCODE_BACK)
//		{
//			//do whatever you want the 'Back' button to do
//			//as an example the 'Back' button is set to start a new Activity named 'NewActivity'
//			finish();
//			return true;
//		}
//		return true;
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		simulation = new SimulationView(this);
		final Button aceleratorButton = (Button) findViewById(R.id.acelerator);
		final Button turboButton = (Button) findViewById(R.id.turbo);
		final Button reverseButton = (Button) findViewById(R.id.reverse);
		final ToggleButton lightsButton = (ToggleButton) findViewById(R.id.lights);
		final ToggleButton connectButton = (ToggleButton) findViewById(R.id.connect);
		final Button blinkButton = (Button) findViewById(R.id.blink);
		final ToggleButton recButton = (ToggleButton) findViewById(R.id.recButton);
		final ToggleButton playButton = (ToggleButton) findViewById(R.id.playButton);
		final Button exitButton = (Button) findViewById(R.id.exitButton);
		final Button editButton = (Button) findViewById(R.id.editorButton);
		aceleratorButton.setEnabled(false);
		turboButton.setEnabled(false);
		reverseButton.setEnabled(false);
		lightsButton.setEnabled(false);
		blinkButton.setEnabled(false);
		lightsButton.setTextOff(null);
		lightsButton.setTextOn(null);
		lightsButton.setText(null);
		connectButton.setTextOn(null);
		connectButton.setTextOff(null);
		connectButton.setText(null);
		recButton.setTextOn(null);
		recButton.setTextOff(null);
		recButton.setText(null);
		recButton.setEnabled(false);
		playButton.setTextOn(null);
		playButton.setTextOff(null);
		playButton.setText(null);
		if(!play){
			playButton.setEnabled(false);
			playButton.setVisibility(View.INVISIBLE);
		}
		recButton.setVisibility(View.INVISIBLE);
		connectButton.setChecked(false);
//		List l = ((List<Integer>) getIntent().getSerializableExtra("listaAction"));
//		if(l!=null){
//			recList = l;
//		}


		anglebound = 28; //Maior angulo que o carro pode dobrar
		
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
		
		position = (TextView) findViewById(R.id.textView);
		status = (TextView) findViewById(R.id.status);
		live = true;
		send = false;
		acelerate = false;
		reverse = false;
		sendRateSleepTime = 100;
		final Thread robot = new Thread(new Robot(mSensorX, mSensorY, mSensorZ, this)); //o primeiro booleando controla o 
		//ciclo de vida e o segundo se envia os dados para o carro ou nao
		robot.start();

		//Recebe o objeto selecionado na classe Scan
		Device dev = (Device) getIntent().getSerializableExtra("device");
		if (dev != null) {
			selectDevice(dev);
			try {
				boolean conectado = openConnection();
				if (conectado) {
					send = true;
					aceleratorButton.setEnabled(true);
					turboButton.setEnabled(true);
					reverseButton.setEnabled(true);
					lightsButton.setEnabled(true);
					blinkButton.setEnabled(true);
					recButton.setEnabled(true);
					connectButton.setChecked(true);
					recButton.setVisibility(View.VISIBLE);
					editButton.setVisibility(View.VISIBLE);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		editButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent motionevent) {
				try {
					Intent i = new Intent(Main.this, Editor.class);
					startActivity(i);

				} catch (Exception e) {

				}
				return true;
			}
		});

		connectButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				boolean on = (connectButton).isChecked();
				if (on) {
					try {
						checkBTState();
						Intent i = new Intent(Main.this, Scan.class);
						startActivity(i);
						Main.this.finish();
					} catch (Exception e) {
						connectButton.setChecked(false);
					}
				} else {
					try {
						closeBT();
						send = false;
						aceleratorButton.setEnabled(false);
						turboButton.setEnabled(false);
						reverseButton.setEnabled(false);
						lightsButton.setEnabled(false);
						blinkButton.setEnabled(false);
						recButton.setEnabled(false);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}

			}
		});



		recButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				boolean on = (recButton).isChecked();
				if (on) {
					recList.clear();
					Main.recording = true;
				} else {
					recording = false;
					byEditor = false;
					playButton.setVisibility(View.VISIBLE);
					playButton.setEnabled(true);
				}

			}
		});



		playButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {

				try {
					boolean on = (playButton).isChecked();
					if (on) {
						send = false;
						workerThread = new Thread(new Runnable() {
							public void run() {
								if (!Thread.currentThread().isInterrupted()) {
									for(Integer b : recList){
										try {
											if(playing) {
												sendData(b);
												System.out.println(b);
												if(!(b >= 150 && b <= 169) && !byEditor) {
													Thread.sleep(sendRateSleepTime);
												}
											}else{
												throw new Exception();
											}
										}catch (Exception e) {
											break;
										}
									}
								}
							}
						});
						workerThread.start();
					}else{
						playing = false;
					}
					try {
						workerThread.join();
					}catch (InterruptedException e){
						//Donothing
					}
					playing = true;
					send = true;
					playButton.setChecked(false);
				}catch (Exception e){
					System.out.println("Lista gravada vazia");
				}

			}
		});

		// Turbo Button

		turboButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent motionevent) {
				int action = motionevent.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					try {
						sendData((byte) 5); //Turn turbo on
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else if (action == MotionEvent.ACTION_UP) {
					try {
						sendData((byte) 6); //Turn turbo off
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}//end else
				return false;
			} //end onTouch
		}); //end b my button

		reverseButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionevent) {
				int action = motionevent.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					reverse = true;
				} else if (action == MotionEvent.ACTION_UP) {
					reverse = false;
					try {
						sendData((byte) 7); //Parar motor
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}//end else
				return false;
			} //end onTouch
		}); //end b my button



		// Lights Button
		lightsButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				boolean on = (lightsButton).isChecked();

				try {
					if (on) {
						sendData((byte) 9); //Ligar luzes
					} else {
						sendData((byte) 8); //Desligar Luzes
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});


		// blink Button
		blinkButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent motionevent) {
				int action = motionevent.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					try {
						sendData((byte) 12); //Ligar luz alta
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else if (action == MotionEvent.ACTION_UP) {
					try {
						sendData((byte) 11); //Desligar farois
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}//end else
				return false;
			} //end onTouch
		}); //end b my button


		exitButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent motionevent) {
				finish();
				return true;
			} //end onTouch
		}); //end b my button
		//Robot Button
//		robotButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
//				boolean on = (robotButton).isChecked();
//
//				if (on) {
//					// Start robot witch send control state to btcar
//					send = true;
//					aceleratorButton.setEnabled(true);
//					turboButton.setEnabled(true);
//					reverseButton.setEnabled(true);
//					lightsButton.setEnabled(true);
//					blinkButton.setEnabled(true);
//
//				} else {
//					// Stop robot witch send control state to btcar
//					send = false;
//					aceleratorButton.setEnabled(false);
//					turboButton.setEnabled(false);
//					reverseButton.setEnabled(false);
//					lightsButton.setEnabled(false);
//					blinkButton.setEnabled(false);
//				}
//
//			}
//		});

		//Acelerator button
		aceleratorButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent motionevent) {
				int action = motionevent.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					acelerate = true;
				} else if (action == MotionEvent.ACTION_UP) {
					acelerate = false;
					try {
						sendData((byte) 7); //Parar motor
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}//end else
				return false;
			} //end onTouch
		}); //end b my button




		if (!checkStatus) {
			status.setText("Disconnected");
		}else {
			status.setText("Connected");
//			robotButton.setEnabled(true);
		}
		simulation.startSimulation();
	}

	public void selectDevice(Device d) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getAddress().equals(d.getMacAddress())){
					mmDevice = device;
					break;
				}
			}
		}
	}


	/**
	 * Metodo que abre a conexao com o dispositivo serial 
	 **/
	public boolean openConnection() throws IOException {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//UUID padrao para dispositivos seriais

		mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);

		mBluetoothAdapter.cancelDiscovery();
		try {
			mmSocket.connect();
			checkStatus = true;
			status.setText("Connected");
		} catch (IOException e2) {
			e2.printStackTrace();
			Toast.makeText(this, "Serial device not found",Toast.LENGTH_SHORT).show();
			checkStatus = false;
			try {
				mmSocket.close();
			} catch (IOException closeException) {
				closeException.printStackTrace();
			}
			return checkStatus;
		}

		Toast.makeText(this, "Connected",Toast.LENGTH_SHORT).show();

		mmOutputStream = mmSocket.getOutputStream();
		mmInputStream = mmSocket.getInputStream();

		beginListenForData();
		return checkStatus;
	}

	void beginListenForData() {
		final Handler handler = new Handler();
		final byte delimiter = 10; // This is the ASCII code for a newline
		// character

		stopWorker = false;
		readBufferPosition = 0;
		readBuffer = new byte[1024];
		workerThread = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted() && !stopWorker) {
					try {
						int bytesAvailable = mmInputStream.available();
						if (bytesAvailable > 0) {
							byte[] packetBytes = new byte[bytesAvailable];
							mmInputStream.read(packetBytes);
							for (int i = 0; i < bytesAvailable; i++) {
								byte b = packetBytes[i];
								if (b == delimiter) {
									byte[] encodedBytes = new byte[readBufferPosition];
									System.arraycopy(readBuffer, 0,
											encodedBytes, 0,
											encodedBytes.length);
									final String data = new String(
											encodedBytes, "US-ASCII");
									readBufferPosition = 0;

									handler.post(new Runnable() {
										public void run() {
											// myLabel.setText(data);
										}
									});
								} else {
									readBuffer[readBufferPosition++] = b;
								}
							}
						}
					} catch (IOException ex) {
						stopWorker = true;
					}
				}
			}
		});

		workerThread.start();
	}

	/**
	 * Metodo que envia dados
	 * @param x 
	 * @throws IOException
	 */
	void sendData(int x) throws IOException {
		mmOutputStream.write((byte) 33);
		mmOutputStream.write((byte)x);
		if(Main.recording){
			Main.recList.add(x);
		}
	}

	/**
	 * Close connection
	 */
	void closeBT() throws IOException {
		stopWorker = true;
		mmOutputStream.close();
		mmInputStream.close();
		mmSocket.close();
		checkStatus = false;
		status.setText("Disconnected");

	}

	public void onDestroy() {
		this.mWakeLock.release();
		super.onDestroy(); 
		
		if (mmSocket != null) { 
			try {
				closeBT();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Checa o estado do Adaptado Bluetooth
	 * Caso nao esteja habilitado sera solicitado a ativacao
	 * caso necessite efetuar a verificacao utilizar o retorno do metodo
	 */
	private boolean checkBTState() {
		boolean retorno;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			alertBox("Fatal Error", "Bluetooth not supported.");
		} else {
			Intent enableBtIntent = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, 0);
		}
		if (mBluetoothAdapter.isEnabled()) {
			retorno = true;
		} else {
			retorno = false;
		}
		return retorno;
	}

	/**
	 * Mostra um alerta com titulo e mensagem passados por parametro
	 * @param title
	 * @param message
	 */
	public void alertBox(String title, String message) {
		new AlertDialog.Builder(this).setTitle(title)
		.setMessage(message + " Press OK to exit.")
		.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		}).show();
	}

	private BroadcastReceiver mReceiver;

	/**
	 * Metodo que discobre novos dispositivos
	 * Nao implementado pois pode ser feito diretamente no S.O. Android
	 */
	public void discoverBT() {
		// Create a BroadcastReceiver for ACTION_FOUND
		mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				// When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					// Add the name and address to an array adapter to show in a
					// ListView
					// mArrayAdapter.add(device.getName() + "\n"+
					// device.getAddress());
				}
			}
		};
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister
		// during onDestroy
	}

	class SimulationView extends View implements SensorEventListener {

		private Sensor mAccelerometer;

		public void startSimulation() {
			mSensorManager.registerListener((SensorEventListener) this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
		}
		public void stopSimulation() {
			mSensorManager.unregisterListener(this);
		}

		public SimulationView(Context context) {
			super(context);
			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
				return;			 
			mSensorX = event.values[2];
			mSensorY = event.values[1];
			mSensorZ = event.values[0];
			//O trcho abaixo será excluido
			position.setText(mSensorX+"\n"+mSensorY+"\n"+mSensorZ);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	}

	class Robot implements Runnable{

		float x, y, z;
		Main m;

		public Robot(float mSensorX, float mSensorY, float mSensorZ, Main m) {
			x = mSensorX;
			y = mSensorY;
			z = mSensorZ;
			this.m = m;
		}

		public void run() {
			while(m.live){
				if(m.send){
					try {
						sendState();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(m.sendRateSleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}



		private void sendState() throws IOException {
			int x;
			if(m.send){
				x = Math.abs((int) mSensorX);
				//Controle da aceleracao
				if(m.reverse){
					if(x > 0){
						if(x < 8){
							m.sendData(160 + x);
						}else{
							m.sendData(169);
						}
					}
				}
				if(m.acelerate){
					System.out.println(x);
						if(x < 8){
							m.sendData(150 + x);
						}else{
							m.sendData(159);
						}
					
				}

				//Controla a direcao
			
				float aux = mSensorY;
				if(aux >= -9 ){
					if(aux < 0){
						//Turn Left
						m.sendData( (int)(50 + Math.abs(aux) * 4));
					}else if(aux >= -0.2 && aux <= 0.2 ){
						//Trun Center
						m.sendData(100); 
					}else if(aux < 9){
						//Turn Right
						m.sendData((int) (100 + aux * 4));
					}else{
						m.sendData(100+anglebound);
					}
				}else{
					m.sendData(50+anglebound);
				}
			}

		}
	}
}
