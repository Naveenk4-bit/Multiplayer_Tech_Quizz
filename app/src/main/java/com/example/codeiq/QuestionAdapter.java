package com.example.codeiq;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

    private List<Question> questionList;

    public QuestionAdapter(List<Question> questionList) {
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Question question = questionList.get(position);
        holder.textViewQuestion.setText(question.getQuestionText());
        holder.textViewOption1.setText("A) " + question.getAnswer(0));
        holder.textViewOption2.setText("B) " + question.getAnswer(1));
        holder.textViewOption3.setText("C) " + question.getAnswer(2));
        holder.textViewOption4.setText("D) " + question.getAnswer(3));

        // Display correct answer
        holder.textViewCorrectAnswer.setText("Correct Answer: " + question.getCorrectAnswerText());
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewQuestion, textViewOption1, textViewOption2, textViewOption3, textViewOption4, textViewCorrectAnswer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewQuestion = itemView.findViewById(R.id.textView_question);
            textViewOption1 = itemView.findViewById(R.id.textView_option1);
            textViewOption2 = itemView.findViewById(R.id.textView_option2);
            textViewOption3 = itemView.findViewById(R.id.textView_option3);
            textViewOption4 = itemView.findViewById(R.id.textView_option4);
            textViewCorrectAnswer = itemView.findViewById(R.id.textView_correct_answer);
        }
    }
}
