Number.prototype.pad = function(decimals) {
   return (new Array(decimals - String(this).length +1).join("0")) + this;
};
