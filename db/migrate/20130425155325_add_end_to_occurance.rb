class AddEndToOccurance < ActiveRecord::Migration
  def change
    change_table :occurances do |t|
      t.column :end, :datetime
    end
  end
end
