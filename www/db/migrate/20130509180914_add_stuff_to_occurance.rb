class AddStuffToOccurance < ActiveRecord::Migration
  def change
    change_table :occurances do |t|
      t.column :domain_definition, :text
      t.column :min_duration, :integer
      t.column :max_duration, :integer
    end
  end
end
