class CreateActivities < ActiveRecord::Migration
  def change
    create_table :activities do |t|
      t.string :name, :null => false
      t.text :description
      t.text :data
      t.string :type, :null => false

      t.timestamps
    end
  end
end
