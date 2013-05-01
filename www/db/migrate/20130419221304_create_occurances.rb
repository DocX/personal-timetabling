class CreateOccurances < ActiveRecord::Migration
  def change
    create_table :occurances do |t|
      t.datetime :start, :null => false
      t.integer :duration, :null => false

      t.references :activity, :index => true
    end
  end
end
