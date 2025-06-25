package com.example.codeiq;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EditableQuestionAdapter extends RecyclerView.Adapter<EditableQuestionAdapter.ViewHolder> {

    private List<EditableQuestion> questionList;

    public EditableQuestionAdapter(List<EditableQuestion> questionList) {
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_editable_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EditableQuestion question = questionList.get(position);
        holder.editTextQuestion.setText(question.getQuestionText());
        holder.editTextOption1.setText(question.getOption1());
        holder.editTextOption2.setText(question.getOption2());
        holder.editTextOption3.setText(question.getOption3());
        holder.editTextOption4.setText(question.getOption4());
        holder.textViewCorrectAnswer.setText("Correct: " + question.getCorrectAnswerText());
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText editTextQuestion, editTextOption1, editTextOption2, editTextOption3, editTextOption4;
        TextView textViewCorrectAnswer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editTextQuestion = itemView.findViewById(R.id.editText_question);
            editTextOption1 = itemView.findViewById(R.id.editText_option1);
            editTextOption2 = itemView.findViewById(R.id.editText_option2);
            editTextOption3 = itemView.findViewById(R.id.editText_option3);
            editTextOption4 = itemView.findViewById(R.id.editText_option4);
            textViewCorrectAnswer = itemView.findViewById(R.id.textView_correct_answer);
        }
    }
}
