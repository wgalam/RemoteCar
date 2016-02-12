package remotecar.com.br.remotecar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Editor extends AppCompatActivity {
    static boolean light = false;
    static boolean forward = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
    final EditText program = (EditText) findViewById(R.id.program);

        final Button nitroButton = (Button) findViewById(R.id.button6);
        nitroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToEnd(program, "005");
            }
        });

        final Button forwardButton = (Button) findViewById(R.id.button);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(forward){
                    forward = false;
                    forwardButton.setText("Forward");
                }else{
                    forward = true;
                    forwardButton.setText("Backward");
                }
            }
        });

        final Button backspaceButton = (Button) findViewById(R.id.backspace);
        backspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (program.getText().toString().length() > 3) {
                    program.setText(program.getText().toString().substring(0, program.getText().toString().length() - 4));
                }
            }
        });

        final EditText turn = (EditText) findViewById(R.id.editText);

        final SeekBar steeringBar = (SeekBar) findViewById(R.id.seekBar3);
        steeringBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override

            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                turn.setText("" + (steeringBar.getProgress() - 50));
            }

            @Override

            public void onStartTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (steeringBar.getProgress() < 50) {
                    addToEnd(program, "0" + (50 + steeringBar.getProgress()));
                } else {
                    addToEnd(program, "" + (50 + steeringBar.getProgress()));
                }

//                textView.setText("Covered: " + progress + "/" + seekBar.getMax());
//                Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }

        });

        final SeekBar aceleratorBar = (SeekBar) findViewById(R.id.seekBar4);
        aceleratorBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {

            }

            @Override

            public void onStartTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                if(forward){
                    addToEnd(program, "" + (150 + aceleratorBar.getProgress()));
                }else{
                    addToEnd(program, "" + (160 + aceleratorBar.getProgress()));
                }
                } catch (Exception e) {

                }

            }

        });




        final Button lightButton = (Button) findViewById(R.id.light);
        lightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!light) {
                        addToEnd(program, "009");
                        lightButton.setText("Lights Off");
                        light = true;
                    } else {
                        addToEnd(program, "008");
                        light = false;
                        lightButton.setText("Lights  On");
                    }

                } catch (Exception e) {

                }
            }
        });

        final Button shortButton = (Button) findViewById(R.id.button3);
        shortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addToEnd(program, "016");
                } catch (Exception e) {

                }
            }
        });

        final Button midButton = (Button) findViewById(R.id.button4);
        midButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addToEnd(program, "017");
                } catch (Exception e) {

                }
            }
        });

        final Button longButton = (Button) findViewById(R.id.button5);
        longButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addToEnd(program, "018");
                } catch (Exception e) {

                }
            }
        });

        final Button blinkButton = (Button) findViewById(R.id.blink);
        blinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addToEnd(program, "012");
                    addToEnd(program, "008");
                } catch (Exception e) {

                }
            }
        });

        final Button stopButton = (Button) findViewById(R.id.button2);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addToEnd(program, "007");
                } catch (Exception e) {

                }
            }
        });

        final Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Integer> list = new ArrayList<Integer>();

                    for(String str : program.getText().toString().split("!")){
                        if(!str.equals("")){
                            list.add(Integer.parseInt(str));
                        }
                    }
                    list.add(007);
                    list.add(8);
                    Main.recList = list;
                    Main.play = true;
                    Intent i = new Intent(Editor.this, Main.class);
//                    i.putExtra("listaAction", (Serializable) list);
                    Main.byEditor = true;
                    startActivity(i);
                    Editor.this.finish();
                } catch (Exception e) {

                }
            }
        });

        final Button cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent i = new Intent(Editor.this, Main.class);
                    startActivity(i);
                    Editor.this.finish();
                } catch (Exception e) {

                }
            }
        });

    }

    protected void addToEnd(EditText seek, String c){
        seek.setText(seek.getText()+"!"+c);
    }

}
