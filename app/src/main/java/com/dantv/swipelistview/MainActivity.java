package com.dantv.swipelistview;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText edtTxt;
    private Button btnAdd;
    private SwipeMenuListView listView;
    private SQLiteDatabase bancoDados;

    private ArrayAdapter<String> itensAdaptador;
    private ArrayList<String> itens;
    private ArrayList<Integer> ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (SwipeMenuListView) findViewById(R.id.listView);
        edtTxt = (EditText) findViewById(R.id.edTxtId);
        btnAdd = (Button) findViewById(R.id.btnAddId);

        //create DB
        bancoDados = openOrCreateDatabase("apptarefasSwipe", MODE_PRIVATE, null);

        //create table
        bancoDados.execSQL("CREATE TABLE IF NOT EXISTS tarefas (id INTEGER PRIMARY KEY AUTOINCREMENT, tarefa VARCHAR)");

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoDigitado = edtTxt.getText().toString();
                salvarTarefa(textoDigitado);
            }
        });

        //delete with a swipe menu
        swipeMenu();

        //delete with a menu
        //viewMenu();

        //delete with a longclick
        //longClick();

        recuperarTarefas();
    }

    private void recuperarTarefas() {
        try {
            //retrieve the tasks
            Cursor cursor = bancoDados.rawQuery("SELECT * FROM tarefas ORDER BY id DESC", null);

            //retrieve columns IDS
            int indiceColunaId = cursor.getColumnIndex("id");
            int indiceColunaTarefa = cursor.getColumnIndex("tarefa");

            //create the adapter
            ids = new ArrayList<>();
            itens = new ArrayList<>();
            itensAdaptador = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_2, android.R.id.text1, itens);
            listView.setAdapter(itensAdaptador);

            //list tasks
            cursor.moveToFirst();
            while (cursor != null) {
                Log.i("Resultado - ", "Tarefa: " + cursor.getString(indiceColunaTarefa));
                itens.add(cursor.getString(indiceColunaTarefa));
                ids.add(Integer.parseInt(cursor.getString(indiceColunaId)));
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void salvarTarefa(String texto) {

        try {
            if (texto.equals("")) {
                Toast.makeText(MainActivity.this, "Digite uma tarefa.", Toast.LENGTH_SHORT).show();
            } else {
                bancoDados.execSQL("INSERT INTO tarefas (tarefa) VALUES ('" + texto + "') ");
                Toast.makeText(MainActivity.this, "Tarefa salva com sucesso.", Toast.LENGTH_SHORT).show();
                recuperarTarefas();
                edtTxt.setText("");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removerTarefa(Integer id) {

        try {
            bancoDados.execSQL("DELETE FROM tarefas WHERE id =" + id);
            recuperarTarefas();
            Toast.makeText(MainActivity.this, "Tarefa removida com sucesso.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //delete with a swipe menu
    private void swipeMenu() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width

                openItem.setWidth(370);

                // set item title
                openItem.setTitle("Delete");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

//                    // create "delete" item
//                    SwipeMenuItem deleteItem = new SwipeMenuItem(
//                            getApplicationContext());
//                    // set item background
//                    deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
//                            0x3F, 0x25)));
//                    // set item width
//                    deleteItem.setWidth(170);
//                    // set a icon
//                    deleteItem.setIcon(R.drawable.ic_phone);
//                    // add to menu
//                    menu.addMenuItem(deleteItem);
            }
        };

        listView.setMenuCreator(creator);
        listView.setCloseInterpolator(new BounceInterpolator());
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        removerTarefa(ids.get(position));
                        break;
                    //case 1:

                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    //delete with a menu
    private void viewMenu() {
        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                            ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.add(0, 1, 0, "deletar");
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = menuInfo.position;
        switch (item.getItemId()) {
            case 1:
                removerTarefa(ids.get(position));
        }
        return super.onContextItemSelected(item);
    }

    //delete with a longclick
    private void longClick() {
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                removerTarefa(ids.get(position));
                return false;
            }
        });
    }

}
