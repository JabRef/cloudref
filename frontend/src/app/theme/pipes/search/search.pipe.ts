import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'searchPipe',
})

export class SearchPipe implements PipeTransform {

  /**
   * Search and order a list of objects.
   * Code adopted from: http://www.angulartutorial.net/2017/03/simple-search-using-pipe-in-angular-2.html
   *
   * @param list the list to search in.
   * @param input the search input.
   * @param searchableList the fields of the objects of list where to search for the input.
   * @param sort the field after which should be sorted.
   * @returns {any} the resulting list.
   */
  transform(list: any, input: string, searchableList: any, sort: string) {
    let result = list;

    if (input != null) {
      if (input) {
        input = input.toLowerCase();
        result = result.filter(function (el: any) {
          let isTrue = false;
          for (let k in searchableList) {
            if (el[searchableList[k]].toLowerCase().indexOf(input) > -1) {
              isTrue = true;
            }
            if (isTrue) {
              return el;
            }
          }
        });

        // sort result
        result = result.sort((obj1, obj2) => {
          if (obj1[sort] > obj2[sort]) {
            return 1;
          }
          if (obj1[sort] < obj2[sort]) {
            return -1;
          }
          return 0;
        });
      }
    }
    return result;
  }

}
