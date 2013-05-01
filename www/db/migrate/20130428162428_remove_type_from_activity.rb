class RemoveTypeFromActivity < ActiveRecord::Migration
  def change
    change_table :activities do |t|
      t.remove :type
    end
  end
end
